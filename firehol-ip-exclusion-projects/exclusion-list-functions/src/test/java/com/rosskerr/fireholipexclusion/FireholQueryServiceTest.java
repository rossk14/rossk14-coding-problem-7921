package com.rosskerr.fireholipexclusion;

import java.util.List;

import com.rosskerr.fireholipexclusion.api.management.ExclusionListPacket;
import com.rosskerr.fireholipexclusion.core.management.ExclusionListManagementService;
import com.rosskerr.fireholipexclusion.core.query.ExclusionListQueryService;

import org.testng.annotations.Test;

@Test(enabled = false)
public class FireholQueryServiceTest {
    
    /**
     * ExclusionListManagementService full functionality Test
     * Must have AWS CLI properly configured so that the AWS auth
     * chain can allow access to the AWS resources needed
     */
    @Test(enabled = false)
    public void TestQueryService() {

        String AWS_REGION = "us-east-1";
        String S3_BUCKET = ""; 
        String S3_KEY = "";
        String testAddress = "";
        int expectedHitCount = 0;

        ExclusionListQueryService service = new ExclusionListQueryService(null);

        ExclusionListPacket exclusionLists = service.getExclusionLists(S3_BUCKET, S3_KEY, AWS_REGION);

        List<String> hits = service.getExclusionListHits(testAddress, exclusionLists);

        if (hits.size() != expectedHitCount) {
            throw new RuntimeException("Expected hit count " + expectedHitCount + "but found " + hits.size());
        }

    }
}
