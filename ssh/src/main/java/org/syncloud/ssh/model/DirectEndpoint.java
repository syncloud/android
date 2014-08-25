package org.syncloud.ssh.model;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;

public class DirectEndpoint implements Serializable {
    private String host;
    private int port;
    private String login;
    private String password;
    private String key;


    public DirectEndpoint(String host, int port, String login, String password, String key) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.key = key;
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

    public String  getDisplayName() {
        return host;
    }

    @Override
    public String toString() {
        return "DeviceEndpoint{" +
                "host='" + host +
                ", port=" + port +
                ", login='" + (isNoneBlank(login) ? "[available]" : "[none]") +
                ", key='" + (isNoneBlank(key) ? "[available]" : "[none]") +
                '}';
    }
}
