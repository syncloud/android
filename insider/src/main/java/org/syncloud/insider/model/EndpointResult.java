package org.syncloud.insider.model;

public class EndpointResult {
    private Endpoint data;
    private String message;
    private Boolean success;

    public Endpoint getData() {
        return data;
    }

    public void setData(Endpoint data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
