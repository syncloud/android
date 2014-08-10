package org.syncloud.insider.model;

public class PortMapping {
    private int external_port;
    private int local_port;

    public PortMapping() {
    }

    public int getExternal_port() {
        return external_port;
    }

    public void setExternal_port(int external_port) {
        this.external_port = external_port;
    }

    public int getLocal_port() {
        return local_port;
    }

    public void setLocal_port(int local_port) {
        this.local_port = local_port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PortMapping that = (PortMapping) o;

        if (local_port != that.local_port) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return local_port;
    }
}
