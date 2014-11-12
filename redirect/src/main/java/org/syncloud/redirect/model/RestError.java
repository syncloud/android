package org.syncloud.redirect.model;

import java.util.List;

public class RestError {
    public String message;
    public List<ParameterMessages> parameters_messages;

    public RestError() {}

    public RestError(String message) {
        this.message = message;
    }
}
