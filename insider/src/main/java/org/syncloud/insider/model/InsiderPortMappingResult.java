package org.syncloud.insider.model;

import org.syncloud.insider.model.PortMapping;

import java.util.List;

public class InsiderPortMappingResult {
    private List<PortMapping> data;
    private String message;
    private Boolean success;

    public List<PortMapping> getData() {
        return data;
    }

    public void setData(List<PortMapping> data) {
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
