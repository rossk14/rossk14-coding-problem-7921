package com.rosskerr.fireholipexclusion.lambdas.management;

import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rosskerr.fireholipexclusion.api.management.ExclusionListPacket;
import com.rosskerr.fireholipexclusion.api.query.ExclusionListQueryResultPacket;
import com.rosskerr.fireholipexclusion.core.LoggingHelper;
import com.rosskerr.fireholipexclusion.core.MetricsPublicationHelper;
import com.rosskerr.fireholipexclusion.core.query.ApiGatewayResponsePacket;
import com.rosskerr.fireholipexclusion.core.query.ExclusionListQueryService;

/**
 * Environmental Vars
 * S3_BUCKET
 * S3_KEY
 */
public class ExclusionListQueryFunction implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayProxyResponseEvent> {

    private final String AWS_REGION = System.getenv("AWS_REGION");

    private final String S3_BUCKET = System.getenv("S3_BUCKET");
    private final String S3_KEY = System.getenv("S3_KEY");

    // create logger

    // hold our exclusion list over multiple invokations to save a little time, 
    // list will only change once a day so we probably won't run into stale data often
    // TODO make sure we don't run into stale data often
    private ExclusionListPacket exclusionLists;


    public ExclusionListQueryFunction() {
        // LoggingHelper LOG = new LoggingHelper(context.getLogger());
        ExclusionListQueryService service = new ExclusionListQueryService(null);
        this.exclusionLists = service.getExclusionLists(S3_BUCKET, S3_KEY, AWS_REGION);
        // LOG.info(String.format("Loaded exclusion list data with %d available exclusion lists.", exclusionLists.getExclusionLists().size()));
    }
    
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        String queryAddress = event.getQueryStringParameters().getOrDefault("address", null);

        LoggingHelper LOG = new LoggingHelper(context.getLogger());
        ExclusionListQueryService service = new ExclusionListQueryService(LOG);

        if (this.exclusionLists == null) {
            // log error
            LOG.error("In handleRequest, no address to lookup.", null);
            // publish failure metric
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_QUERY", "Queries", "MisingAddressFailures", 1);
            // return failure packet
            String result;
            int status = 200;
            try {
                result = new ObjectMapper().writeValueAsString(new ExclusionListQueryResultPacket()
                    .withQueriedAddress(queryAddress)
                    .withFailureMessage("No address for comparison. Please use querystring param 'address' to send address.")
                    .withSuccess(false));
            } catch (Exception ex2) {
                LOG.error("Couldn't serialize response to json.", ex2);
                result = "Error serializing response";
                status = 500;
            }

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withIsBase64Encoded(false)
                .withBody(result);
        }

        if (this.exclusionLists == null) {
            // log error
            LOG.error("In handleRequest, no exclusion lists available.", null);
            // publish failure metric
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_QUERY", "Queries", "MisingExclusionListFailures", 1);
            // return failure packet
            String result;
            int status = 200;
            try {
                result = new ObjectMapper().writeValueAsString(new ExclusionListQueryResultPacket()
                    .withQueriedAddress(queryAddress)
                    .withFailureMessage("No exclusion lists available.")
                    .withSuccess(false));
            } catch (Exception ex2) {
                LOG.error("Couldn't serialize response to json.", ex2);
                result = "Error serializing response";
                status = 500;
            }

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withIsBase64Encoded(false)
                .withBody(result);
        }

        try {
            List<String> hits = service.getExclusionListHits(queryAddress, this.exclusionLists);
            LOG.info(String.format("Successfully processed address '%s', %d hits found.", queryAddress, hits.size()));
            LOG.debug(String.format("%d total exclusion lists searched.", this.exclusionLists.getExclusionLists().size()));
            // log success metric
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_QUERY", "Queries", "Successes", 1);
            // if there were hits, log excluded address found, otherwise report 0 excluded address hits
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_QUERY", "Queries", "ExludedAddressHits", hits.size() > 0 ? 1 : 0);

            String result = new ObjectMapper().writeValueAsString(new ExclusionListQueryResultPacket()
                .withQueriedAddress(queryAddress)
                .withSuccess(true)
                .withHits(hits));

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(false)
                .withBody(result);
        } catch (Exception ex) {
            // log error
            LOG.error(String.format("In handleRequest, failure searching for address '%s' in %d exclusion lists.", 
                event, 
                this.exclusionLists.getExclusionLists().size()), null);
            // publish failure metric
            new MetricsPublicationHelper().publishToMetric("FIREHOL_LIST_QUERY", "Queries", "Failures", 1);
            // return failure packet
            String result;
            int status = 200;
            try {
                result = new ObjectMapper().writeValueAsString(new ExclusionListQueryResultPacket()
                    .withQueriedAddress(queryAddress)
                    .withFailureMessage("Failure searching for address '{}' in {} exclusion lists.")
                    .withSuccess(false));
            } catch (Exception ex2) {
                LOG.error("Couldn't serialize response to json.", ex2);
                result = "Error serializing response";
                status = 500;
            }

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withIsBase64Encoded(false)
                .withBody(result);
        }
    }
    
}