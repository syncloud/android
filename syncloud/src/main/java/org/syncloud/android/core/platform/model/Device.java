package org.syncloud.android.core.platform.model;

import com.google.common.base.Optional;

import java.io.Serializable;

public class Device implements Serializable {
    private Identification id;
    private Endpoint remoteEndpoint;
    private Endpoint localEndpoint;

    public Device(Identification id, Endpoint localEndpoint, Endpoint remoteEndpoint) {
        this.id = id;
        this.localEndpoint = localEndpoint;
        this.remoteEndpoint = remoteEndpoint;
    }

    public Endpoint localEndpoint() { return localEndpoint; }

    public Endpoint remoteEndpoint() { return remoteEndpoint; }

    public Identification id() { return id; }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", remoteEndpoint=" + remoteEndpoint +
                ", localEndpoint=" + localEndpoint +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device other = (Device) o;

        if (id != null ? !id.equals(other.id) : other.id != null) return false;
        if (localEndpoint != null ? !localEndpoint.equals(other.localEndpoint) : other.localEndpoint != null)
            return false;
        if (remoteEndpoint != null ? !remoteEndpoint.equals(other.remoteEndpoint) : other.remoteEndpoint != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (remoteEndpoint != null ? remoteEndpoint.hashCode() : 0);
        result = 31 * result + (localEndpoint != null ? localEndpoint.hashCode() : 0);
        return result;
    }
}
