package org.syncloud.ssh.model;

import java.io.Serializable;

import static com.google.common.collect.Iterables.filter;
import static org.apache.commons.lang3.StringUtils.join;

public class Device implements Serializable {
    private Integer id;
    private String  userDomain;
    private DirectEndpoint localEndpoint;
    private String name;

    public Device(Integer id, String name, String userDomain, DirectEndpoint localEndpoint) {
        this.userDomain = userDomain;
        this.localEndpoint = localEndpoint;
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getDisplayName() {
        return name != null ? name : userDomain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public DirectEndpoint getLocalEndpoint() {
        return localEndpoint;
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
