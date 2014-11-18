package org.syncloud.ssh.model;

import java.io.Serializable;

public class Device implements Serializable {
    private Integer id;
    private String  userDomain;
    private Endpoint localEndpoint;
    private Credentials credentials;

    public Device(Integer id, String userDomain, Endpoint localEndpoint, Credentials credentials) {
        this.userDomain = userDomain;
        this.localEndpoint = localEndpoint;
        this.id = id;
        this.credentials = credentials;
    }

    public Integer getId() {
        return id;
    }

    public String getDisplayName() {
        return userDomain;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public Endpoint getLocalEndpoint() {
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

        if (id != null ? !id.equals(device.id) : device.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
