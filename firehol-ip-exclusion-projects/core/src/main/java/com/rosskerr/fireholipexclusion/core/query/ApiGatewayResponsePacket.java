package com.rosskerr.fireholipexclusion.core.query;

import java.util.HashMap;
import java.util.Map;

public class ApiGatewayResponsePacket {
    // {
    //     "isBase64Encoded": true|false,
    //     "statusCode": httpStatusCode,
    //     "headers": { "headerName": "headerValue", ... },
    //     "body": "..."
    // }

    private boolean isBase64Encoded;
    private int statusCode;
    private Map<String,String> headers;
    private String body;

    public ApiGatewayResponsePacket withBody(String value) {
        this.body = value;
        return this;
    }
    public ApiGatewayResponsePacket withDefaults() {
        this.isBase64Encoded = false;
        this.statusCode = 200;
        this.headers = new HashMap<>();
        return this;
    }

    public boolean isBase64Encoded() {
        return isBase64Encoded;
    }
    public void setBase64Encoded(boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }
    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
}
