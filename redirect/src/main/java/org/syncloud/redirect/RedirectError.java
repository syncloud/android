package org.syncloud.redirect;

import org.syncloud.redirect.model.ParameterMessages;
import org.syncloud.redirect.model.RestError;

import java.util.List;

public class RedirectError extends RuntimeException {
    public boolean expected;
    public List<ParameterMessages> parameters_messages;
    public Throwable throwable;

    public RedirectError(String message) {
        super(message);
    }

    public RedirectError(String message, Throwable throwable) {
        super(message, throwable);
        this.throwable = throwable;
    }

    public RedirectError(boolean expected, RestError error) {
        super(error.message);
        this.expected = expected;
        this.parameters_messages = error.parameters_messages;
    }
}
