package com.rosskerr.fireholipexclusion.lambdas.management;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.rosskerr.fireholipexclusion.api.management.ExclusionListPacket;
import com.rosskerr.fireholipexclusion.core.LoggingHelper;
import com.rosskerr.fireholipexclusion.core.management.ExclusionListManagementService;

/**
 * Environmental Vars
 * GITHUB_PROJECT_URL
 * S3_BUCKET
 * S3_KEY
 */
public class ExclusionListPopulationFunction implements RequestHandler<Object, Boolean> {

    private final String AWS_REGION = System.getenv("AWS_REGION");

    private final String S3_BUCKET = System.getenv("S3_BUCKET");
    private final String S3_KEY = System.getenv("S3_KEY");

    private final String GIT_CLONE_OUTPUT_FOLDER = "/tmp/firehol";

	@Override
	public Boolean handleRequest(Object event, Context context) {

        // create logger
        LoggingHelper LOG = new LoggingHelper(context.getLogger());

        LOG.info("Preparing to process the latest Firehol exclusion list data.");

        ExclusionListManagementService service = new ExclusionListManagementService(LOG);

        // do github pull
        if (service.pullFireholProject(System.getenv("GITHUB_PROJECT_URL"), GIT_CLONE_OUTPUT_FOLDER)) {
            // check for github directory and pull files
            // process all .ipset and .netset files in GIT_CLONE_OUTPUT_FOLDER
            LOG.info("Preparing to process Firehol exclusion list data...");
            ExclusionListPacket packet = service.processFireholAddressSets(GIT_CLONE_OUTPUT_FOLDER);

            if (packet != null) {
                // store ExclusionListPacket in S3
                service.storeExclusionListsInS3(packet, AWS_REGION, S3_BUCKET, S3_KEY);

                // success!
                LOG.info("Successfully processed the latest Firehol exclusion list data.");
                return true;
            }
        } 

        LOG.warn("Did not successfully pull project to build Firehol exclusion list.");
        // failure of some sort, logged in core service 
        return false;
    }
    
}