package org.syncloud.ssh.model;

import java.io.Serializable;

public class IdentifiedEndpoint implements Serializable {
    private Endpoint enpoint;
    private Identification id;

    public IdentifiedEndpoint(Endpoint endpoint, Identification id) {
        this.enpoint = endpoint;
        this.id = id;
    }

    public Endpoint endpoint() {
        return enpoint;
    }

    public Identification id() {
        return id;
    }
}
