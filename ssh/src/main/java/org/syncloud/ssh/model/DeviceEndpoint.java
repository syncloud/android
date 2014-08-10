package org.syncloud.ssh.model;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;

public class DeviceEndpoint implements Serializable {
    private String host;
    private int port;
    private String login;
    private String password;
    private String key;

    public DeviceEndpoint(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public DeviceEndpoint(String host, int port, String login, String password, String key) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.key = key;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "DeviceEndpoint{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", login='" + (isNoneBlank(login) ? "[available]" : "[none]") + '\'' +
                ", key='" + (isNoneBlank(key) ? "[available]" : "[none]") + '\'' +
                '}';
    }
}
