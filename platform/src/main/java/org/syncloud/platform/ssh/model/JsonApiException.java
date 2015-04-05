package org.syncloud.platform.ssh.model;

import org.syncloud.common.SyncloudException;

public class JsonApiException extends SyncloudException {
    public SshShortResult result;

    public JsonApiException(String message, SshShortResult result) {
        super(message);
        this.result = result;
    }
}
