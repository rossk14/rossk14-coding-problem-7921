package com.rosskerr.fireholipexclusion;

import com.rosskerr.fireholipexclusion.api.management.ExclusionListPacket;
import com.rosskerr.fireholipexclusion.core.management.ExclusionListManagementService;

import org.testng.annotations.Test;

@Test(enabled = false)
public class FireholPopulationServiceTest {
    
    /**
     * ExclusionListManagementService full functionality Test
     * Must have AWS CLI properly configured so that the AWS auth
     * chain can allow access to the AWS resources needed
     */
    @Test(enabled = false)
    public void TestPopulationService() {

        String gitHubProjectUrl = "https://github.com/firehol/blocklist-ipsets.git";
        String cloneIntoFolder = "C:\\Temp\\firehol";
        String AWS_REGION = "us-east-1";
        String S3_BUCKET = ""; 
        String S3_KEY = "";

        ExclusionListManagementService service = new ExclusionListManagementService(null);

        // do github pull
        if (service.pullFireholProject(gitHubProjectUrl, cloneIntoFolder)) {
            // check for github directory and pull files
            // process all .ipset and .netset files in GIT_CLONE_OUTPUT_FOLDER,
            ExclusionListPacket packet = service.processFireholAddressSets(cloneIntoFolder);

            if (packet != null) {
                // store ExclusionListPacket in S3
                service.storeExclusionListsInS3(packet, AWS_REGION, S3_BUCKET, S3_KEY);

                // success!
                return;
            }
        }
        throw new RuntimeException("Failed to complete ExclusionListManagementService test");
    }
}
