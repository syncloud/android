package org.syncloud.model;

import java.io.Serializable;

public class Device implements Serializable {
    private String host;
    private int port;
    private String login = "root";
    private String password = "syncloud";
    private String key;
    private String name;

    public Device(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Device(String host, int port, String key) {
        this.key = key;
        this.host = host;
        this.port = port;
    }

    public Device(String host, int port, String key, String name) {
        this.key = key;
        this.host = host;
        this.port = port;
        this.name = name;
    }

    public String getDisplayName() {
        String address = getHost() + ":" + getPort();
        return getName() != null ? getName() : address;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Device{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (port != device.port) return false;
        if (host != null ? !host.equals(device.host) : device.host != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
