package org.syncloud.platform.ssh.model;

public class ConnectionPoint {
    private Endpoint endpoint;
    private Credentials credentials;

    public ConnectionPoint(Endpoint endpoint, Credentials credentials) {
        this.endpoint = endpoint;
        this.credentials = credentials;
    }

    public Endpoint endpoint() {
        return endpoint;
    }

    public Credentials credentials() {
        return credentials;
    }
}
