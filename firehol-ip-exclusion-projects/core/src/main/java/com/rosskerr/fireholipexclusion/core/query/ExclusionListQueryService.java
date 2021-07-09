package com.rosskerr.fireholipexclusion.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.web.util.matcher.IpAddressMatcher;

import com.rosskerr.fireholipexclusion.api.management.BlockedIpCidr;
import com.rosskerr.fireholipexclusion.api.management.BlockedIpCidrSet;
import com.rosskerr.fireholipexclusion.api.management.ExclusionListPacket;
import com.rosskerr.fireholipexclusion.core.LoggingHelper;

public class ExclusionListQueryService {
    
    LoggingHelper LOG;
    public ExclusionListQueryService(LoggingHelper LOG) {
        this.LOG = LOG;
    }

    public ExclusionListPacket getExclusionLists(String s3Bucket, String s3Key, String s3Region) {

        // get the object from S3, deserialize it, and return it
        try {
            ExclusionListPacket result = new ExclusionListPacket();

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(s3Region)
                    .build();

            // get a list of all the objects
            ListObjectsV2Result results = s3Client.listObjectsV2(s3Bucket);
            for (S3ObjectSummary summary: results.getObjectSummaries()) {
                try {
                    // read each list from s3, deserialize, and add to our result packet
                    S3Object jsonObject = s3Client.getObject(new GetObjectRequest(s3Bucket, summary.getKey()));
                    BlockedIpCidrSet set = new ObjectMapper().readValue(jsonObject.getObjectContent(), 
                        new TypeReference<BlockedIpCidrSet>() { });
                    result = result.addExclusionList(set);
                } catch (Exception ex) {
                    // log
                    // LOG.warning("Unable to process List packet from " + summary.getKey());
                }
            }

            // LOG.info("Successfully retrieved the Exclusion List packet");
            return result;

        } catch (Exception ex) {
            // LOG.severe("Failed to retrieve the Exclusion List packet. " + ex.getMessage());
            return null;
        }
    }

    public List<String> getExclusionListHits(String addressToTest, ExclusionListPacket lists) {
        if (this.LOG != null) {
            LOG.info("Looking for " + addressToTest + " in " + lists.getExclusionLists().size() + " exclusion lists...");
        }
        List<String> results = new ArrayList<>();
        for (BlockedIpCidrSet l: lists.getExclusionLists()) {
            for (BlockedIpCidr a: l.getBlockedAddresses()) {
                IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(a.getBlockedAddress());
                if (ipAddressMatcher.matches(addressToTest)) {
                    results.add(l.getSetName());
                }
            }
        }
        if (this.LOG != null) {
            LOG.info("Successfully finished search for " + addressToTest + ". Hits: " + results.size());
        }
        return results;
    }
}
