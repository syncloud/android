package org.syncloud.ssh.model;

import java.io.Serializable;

public class Device implements Serializable {
    private String macAddress;
    private String  userDomain;
    private Endpoint localEndpoint;
    private Credentials credentials;
    private Identification id;

    public Device(String macAddress, Identification id, String userDomain, Endpoint localEndpoint, Credentials credentials) {
        this.macAddress = macAddress;
        this.userDomain = userDomain;
        this.localEndpoint = localEndpoint;
        this.credentials = credentials;
        this.id = id;
    }

    public String macAddress() {
        return macAddress;
    }

    public Identification id() {
        return id;
    }

    public String userDomain() {
        return userDomain;
    }

    public Endpoint localEndpoint() {
        return localEndpoint;
    }

    public Credentials credentials() {
        return credentials;
    }

    @Override
    public String toString() {
        return "Device{" +
                "localEndpoint=" + localEndpoint +
                ", userDomain=" + userDomain +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (macAddress != null ? !macAddress.equals(device.macAddress) : device.macAddress != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return macAddress != null ? macAddress.hashCode() : 0;
    }
}
