package org.syncloud.redirect;

import org.syncloud.redirect.model.ParameterMessages;
import org.syncloud.redirect.model.RestError;

import java.util.List;

public class Error extends RestError {
    public boolean expected;
    public String message;
    public List<ParameterMessages> parameters_messages;
    public Throwable throwable;

    public Error(String message) {
        this.message = message;
    }

    public Error(String message, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
    }

    public Error(boolean expected, RestError error) {
        this.expected = expected;
        this.message = error.message;
        this.parameters_messages = error.parameters_messages;
    }
}
