package org.syncloud.redirect.model;

public class RedirectApiException extends RedirectException {
    public RestResult result;

    public RedirectApiException(String message, RestResult result) {
        super(message);
        this.result = result;
    }
}
