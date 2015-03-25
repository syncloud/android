package org.syncloud.platform.ssh.model;

import com.google.common.base.Optional;

import java.io.Serializable;

public class IdentifiedEndpoint implements Serializable {
    private Endpoint enpoint;
    private Optional<Identification> id;

    public IdentifiedEndpoint(Endpoint endpoint, Optional<Identification> id) {
        this.enpoint = endpoint;
        this.id = id;
    }

    public Endpoint endpoint() {
        return enpoint;
    }

    public Optional<Identification> id() {
        return id;
    }
}
