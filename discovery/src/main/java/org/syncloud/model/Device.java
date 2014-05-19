package org.syncloud.model;

import org.syncloud.ssh.Ssh;

import java.io.Serializable;

public class Device implements Serializable {
    private String ip;
    private int port;
    private String login = Ssh.USERNAME;
    private String password = Ssh.PASSWORD;
    private String key;

    public Device(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Device(String ip, int port, String key) {
        this.key = key;
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (port != device.port) return false;
        if (ip != null ? !ip.equals(device.ip) : device.ip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
