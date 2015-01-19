package org.syncloud.ssh.model;

import org.syncloud.redirect.model.Domain;
import org.syncloud.redirect.model.Service;

import java.io.Serializable;

public class Device implements Serializable {
    private String  userDomain;
    private Endpoint remoteEndpoint;
    private Endpoint localEndpoint;
    private Credentials credentials;
    private Identification id;

    public Device(Identification id, String userDomain, Endpoint localEndpoint, Endpoint remoteEndpoint, Credentials credentials) {
        this.id = id;
        this.userDomain = userDomain;
        this.localEndpoint = localEndpoint;
        this.remoteEndpoint = remoteEndpoint;
        this.credentials = credentials;
    }

    public Device (Domain domain, Key key) {
        Service sshService = domain.service("ssh");
        Endpoint localEndpoint = new Endpoint(domain.local_ip, sshService.local_port);
        Endpoint remoteEndpoint = new Endpoint(domain.ip, sshService.port);
        Identification identification = new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        String keyValue = null;
        if (key != null)
            keyValue = key.key;
        Credentials credentials = new Credentials("root", "syncloud", keyValue);

        this.id = identification;
        this.userDomain = domain.user_domain;
        this.localEndpoint = localEndpoint;
        this.remoteEndpoint = remoteEndpoint;
        this.credentials = credentials;
    }


    public String macAddress() { return id.mac_address; }

    public String name() { return id.name; }

    public String title() { return id.title; }

    public String userDomain() {
        return userDomain;
    }

    public Endpoint localEndpoint() {
        return localEndpoint;
    }

    public Endpoint remoteEndpoint() {
        return remoteEndpoint;
    }

    public Identification id() {
        return id;
    }

    public Credentials credentials() {
        return credentials;
    }



    @Override
    public String toString() {
        return "Device{" +
                "localEndpoint=" + localEndpoint +
                ", remoteEndpoint=" + remoteEndpoint +
                ", userDomain=" + userDomain +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (macAddress() != null ? !macAddress().equals(device.macAddress()) : device.macAddress() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return macAddress() != null ? macAddress().hashCode() : 0;
    }
}
