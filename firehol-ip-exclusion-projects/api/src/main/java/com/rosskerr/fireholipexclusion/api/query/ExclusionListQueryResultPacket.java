package com.rosskerr.fireholipexclusion.api.query;

import java.util.ArrayList;
import java.util.List;

public class ExclusionListQueryResultPacket {
    private String queriedAddress;
    private List<String> listHits = new ArrayList<>();
    private Boolean success;
    private String failureMessage;

    /// fluent interface
    public ExclusionListQueryResultPacket withQueriedAddress(String value) {
        this.queriedAddress = value;
        return this;
    }
    public ExclusionListQueryResultPacket withHits(List<String> value) {
        this.listHits.clear();
        this.listHits.addAll(value);
        return this;
    }    
    public ExclusionListQueryResultPacket addHit(String value) {
        this.listHits.add(value);
        return this;
    }
    public ExclusionListQueryResultPacket withSuccess(Boolean value) {
        this.success = value;
        return this;
    }
    public ExclusionListQueryResultPacket withFailureMessage(String value) {
        this.failureMessage = value;
        return this;
    }


    /// hitcount
    public Integer hitCount() {
        return (listHits != null) ? listHits.size() : 0;
    }


    /// getters/setters
    public String getQueriedAddress() {
        return queriedAddress;
    }
    public void setQueriedAddress(String queriedAddress) {
        this.queriedAddress = queriedAddress;
    }
    public List<String> getListHits() {
        return listHits;
    }
    public void setListHits(List<String> listHits) {
        this.listHits = listHits;
    }
    public Boolean getSuccess() {
        return success;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    public String getFailureMessage() {
        return failureMessage;
    } 
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}
