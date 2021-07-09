package com.rosskerr.fireholipexclusion.core.management;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.amazonaws.protocol.json.JsonContent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rosskerr.fireholipexclusion.api.management.BlockedIpCidr;
import com.rosskerr.fireholipexclusion.api.management.BlockedIpCidrSet;
import com.rosskerr.fireholipexclusion.api.management.ExclusionListPacket;
import com.rosskerr.fireholipexclusion.core.LoggingHelper;
import com.rosskerr.fireholipexclusion.core.MetricsPublicationHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ExclusionListManagementService {

    // we wait an appropriate amount of time for a process to finish before checking
    // it for output, but in the exceptional case it takes longer than we expect give
    // it two more tries
    private final int PROCESS_READER_MAX_TRIES = 2;

    private LoggingHelper LOG;
    public ExclusionListManagementService(LoggingHelper LOG) {
        this.LOG = LOG;
    }

    /**
     * Returns null on failure
     * Failure condition is published to cloudwatch metrics
     */
    public ExclusionListPacket processFireholAddressSets(String inputFolder) {
        // check for github directory and pull files
        // process all .ipset and .netset files in GIT_CLONE_OUTPUT_FOLDER,
        // for extra credit do it recursively to get all folders
        ExclusionListPacket packet = new ExclusionListPacket();
        try {
            List<File> files = Arrays.asList(new File(inputFolder).listFiles());
            if (files.size() < 1) {
                throw new Exception("ERROR Git Pull operation must have failed, no files to process");
            }
            files.stream()
                .forEach(f -> {
                    if (f.isFile() && (f.getName().endsWith(".ipset") || f.getName().endsWith(".netset"))) {
                        LOG.debug("Loading file " + f.getName());
                        BlockedIpCidrSet newSet = new BlockedIpCidrSet()
                            .withSetName(f.getName());
                        try {
                            Scanner sc = new Scanner(f);
                            while (sc.hasNextLine()) {
                                String line = sc.nextLine();
                                if (!line.startsWith("#") && !com.amazonaws.util.StringUtils.isNullOrEmpty(line)) {
                                    // good record, save it to our collection
                                    newSet.addBlockedAddress(new BlockedIpCidr()
                                        .withBlockedAddress(line));
                                }
                            }
                            sc.close();
                        } catch (Exception ex) {
                            // catch filenotfound exception and others
                            // log error and continue processing
                            LOG.error(String.format("WARN Error processing expected file '%s': %s.", 
                                f.getName(),
                                ex.getMessage()), ex);
                        }
                        // process files into ExclusionListPacket 
                        packet.addExclusionList(newSet);

                    }
            });

            // Metrics!!!
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "SuccessfulImportProcesses", 
                1); // one successful run
            // new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
            //     "Imports", 
            //     "TotalListsIngested", 
            //     packet.getExclusionLists().size());  
            // publish failure metric to put it on the cloudwatch metrics radar
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "GitProcessingFailures", 
                0); // zero failures               

            // success!
            LOG.info("Successfully processed the latest Firehol exclusion list data.");
            return packet;

        } catch (Exception ex) {
            // log / publish failure metric return
            LOG.error(String.format("Failed to process the Firehol data: %s.", 
                ex.getMessage()), ex);
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "GitProcessingFailures", 
                1);                 
            return null;
        }
    }

    public Boolean storeExclusionListsInS3(ExclusionListPacket packet, 
        String s3Region, 
        String s3Bucket, 
        String s3Key) 
    {
        // store ExclusionListPacket in S3
        LOG.debug(String.format("Storing %d lists in S3.", packet.getExclusionLists().size()));
        try {
            int listFailures = 0;
            int listSuccesses = 0;
            for (BlockedIpCidrSet set: packet.getExclusionLists()) {
                try {
                    // serialize our exclusion list into json then bytes
                    String packetJson = new ObjectMapper().writeValueAsString(set);
                    byte[] packetBytes = packetJson.getBytes();
                    // save it to s3
                    AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                        .withRegion(s3Region)
                        .build();
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentType("application/json");
                    metadata.setContentLength(packetBytes.length);
                    s3.putObject(new PutObjectRequest(s3Bucket, set.getSetName(), new ByteArrayInputStream(packetBytes), metadata));
                    listSuccesses++;
                } catch (Exception ex) {
                    listFailures++;
                    // log failure and keep trucking
                    LOG.warn(String.format("Failed to save list \"%s\" with error: \"%s\".", 
                        set.getSetName(), 
                        ex.getMessage()));
                }
            }
            
            // publish failure metric as zero since we succeeded
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "S3Failures", 
                listFailures);  
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "S3SuccessfulListPuts", 
                listSuccesses); 
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "GenericFailures", 
                0);      
                
            LOG.debug(String.format("Successfully stored %d of %d lists in S3.", listSuccesses, packet.getExclusionLists().size()));
            return true;
    
        } catch (Exception ex) {
            // log / publish failure metric return
            LOG.error(String.format("ERROR Failed to save the updated exclusion data to S3: %s.", 
                ex.getMessage()), ex);
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_MANAGER", 
                "Imports", 
                "GenericFailures", 
                1);                
            return false;
        }
    }

    public Boolean pullFireholProject(String gitProjectUrl, String cloneLocation) {
        try
        {
            // clean tmp workspace
            try {
                FileUtils.deleteDirectory(new File(cloneLocation));
            } catch (Exception ex) {
                // not a real error, if the lambda was not being reused then this folder won't exist
                LOG.debug("Unable to remove old data at " + cloneLocation);
            }

            // perform git pull
            List<String> args = new ArrayList<>();
            args.add("git");
            args.add("clone");
            args.add(gitProjectUrl);
            args.add(cloneLocation);
            
            Process process = new ProcessBuilder()
                    .command(args)
                    .redirectErrorStream(true)
                    .redirectOutput(Redirect.PIPE)
                    .start();

            String output = "Output from Process -- \n";
            InputStream reader = process.getInputStream();
            if (reader == null)
            {
                LOG.warn("WARN: Trying to read output from process but got NULL reader...");
            }
            process.waitFor(30, TimeUnit.SECONDS); // should not take half a minute to pull this repo
            int availableBytes = reader.available();
            LOG.debug("Reading " + availableBytes + " bytes of data...");
            int tries = 0;

            // it might not be ready immediately so wait for another 5 seconds
            while (availableBytes > 0 && tries < PROCESS_READER_MAX_TRIES)
            {
                byte[] bytes = new byte[availableBytes];
                reader.read(bytes);
                output = output.concat(new String(bytes));
                availableBytes = reader.available();
                LOG.debug("Reading " + availableBytes + " bytes of data...");
                if (availableBytes == 0)
                {
                    tries++;
                }
                Thread.sleep(5000); // give it 5 seconds
            }

            LOG.info("Done downloading Firehol exclusion data");

            return true;

        } catch (Exception ex) {
            LOG.error(String.format("ERROR: problem performing Git Pull on '%s': %s.", 
                gitProjectUrl, 
                ex.getMessage()), ex);
            return false;
        }

    }
    
}