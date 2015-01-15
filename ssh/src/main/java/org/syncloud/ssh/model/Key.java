package org.syncloud.ssh.model;

public class Key {
    public String macAddress;
    public String key;

    public Key(String macAddress, String key) {
        this.macAddress = macAddress;
        this.key = key;
    }

}
