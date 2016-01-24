package org.syncloud.android.core.platform.model;

import java.io.Serializable;

public class Endpoint implements Serializable {
    private String host;
    private int port;


    public Endpoint(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
