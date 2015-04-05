package org.syncloud.redirect.model;

import org.syncloud.common.SyncloudException;

public class RedirectApiException extends SyncloudException {
    public RestResult result;

    public RedirectApiException(String message, RestResult result) {
        super(message);
        this.result = result;
    }
}
