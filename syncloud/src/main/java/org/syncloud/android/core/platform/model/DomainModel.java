package org.syncloud.android.core.platform.model;

import org.syncloud.android.core.redirect.model.Domain;

import java.io.Serializable;

public class DomainModel implements Serializable {
    private String  userDomain;
    private Device device;

    private static Identification deviceId(Domain domain) {
        if (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null)
            return new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        return null;
    }

    public DomainModel(Domain domain) {
        Identification id = deviceId(domain);

        if (id != null) {
            Endpoint localEndpoint = null;
            if (domain.local_ip != null && domain.web_local_port != null)
                localEndpoint = new Endpoint(domain.local_ip, domain.web_local_port);

            Endpoint remoteEndpoint= null;
            if (domain.ip != null && domain.web_port != null)
                remoteEndpoint = new Endpoint(domain.ip, domain.web_port);

            this.device = new Device(id, localEndpoint, remoteEndpoint);
            this.userDomain = domain.user_domain;
        }
    }

    public String userDomain() {
        return userDomain;
    }

    public Device device() { return device; }

    @Override
    public String toString() {
        return "DomainModel{" + "userDomain='" + userDomain + '\'' + ", device=" + device + '}';
    }

    public boolean hasDevice() {
        return device != null;
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
