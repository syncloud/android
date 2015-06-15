package org.syncloud.platform.ssh.model;

public class Key {
    public String macAddress;
    public Credentials credentials;

    public Key(String macAddress, Credentials credentials) {
        this.macAddress = macAddress;
        this.credentials = credentials;
    }
}
