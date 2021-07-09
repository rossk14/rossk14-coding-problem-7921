package com.rosskerr.fireholipexclusion.core;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class MetricsPublicationHelper {
    /**
     * Publishes the provided metric to Cloudwatch
     */
    public boolean publishToMetric(
        String metricName, // FIREHOL_LIST_MANAGER
        String dimensionName, // LIST_LOADS
        String valueName, // FAILURES
        Integer count) 
    {
        final AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.defaultClient();
        
        Dimension dimension = new Dimension()
            .withName(dimensionName)
            .withValue(valueName);
        
        MetricDatum datum = new MetricDatum()
            .withMetricName(metricName)
            .withUnit(StandardUnit.Count)
            .withValue(count.doubleValue())
            .withDimensions(dimension);
        
        PutMetricDataRequest request = new PutMetricDataRequest()
            .withNamespace("RossKerr/Firehol")
            .withMetricData(datum);
        
        PutMetricDataResult response = cw.putMetricData(request);
        return true;
    }
}

