package org.syncloud.platform.ssh.model;

public class JsonApiException extends RuntimeException {
    public SshShortResult result;

    public JsonApiException(String message, SshShortResult result) {
        super(message);
        this.result = result;
    }
}
