package org.syncloud.ssh.model;

import java.io.Serializable;

public class DomainModel implements Serializable {
    private String  userDomain;
    private Device device;

    public DomainModel(String userDomain, Device device) {
        this.userDomain = userDomain;
        this.device = device;
    }

    public String userDomain() {
        return userDomain;
    }

    public Device device() { return device; }

    @Override
    public String toString() {
        return "DomainModel{" + "userDomain='" + userDomain + '\'' + ", device=" + device + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainModel that = (DomainModel) o;

        if (device != null ? !device.equals(that.device) : that.device != null) return false;
        if (userDomain != null ? !userDomain.equals(that.userDomain) : that.userDomain != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userDomain != null ? userDomain.hashCode() : 0;
        result = 31 * result + (device != null ? device.hashCode() : 0);
        return result;
    }
}
