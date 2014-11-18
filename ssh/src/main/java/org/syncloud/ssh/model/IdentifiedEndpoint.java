package org.syncloud.ssh.model;

public class IdentifiedEndpoint {
    private Endpoint enpoint;
    private Id id;

    public IdentifiedEndpoint(Endpoint endpoint, Id id) {
        this.enpoint = endpoint;
        this.id = id;
    }

    public Endpoint endpoint() {
        return enpoint;
    }

    public Id id() {
        return id;
    }
}
