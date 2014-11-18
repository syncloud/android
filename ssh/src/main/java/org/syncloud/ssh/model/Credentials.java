package org.syncloud.ssh.model;

import java.io.Serializable;

public class Credentials implements Serializable {

    public static Credentials getStandardCredentials() {
        return new Credentials("root", "syncloud", null);
    }

    private String login;
    private String password;
    private String key;

    public Credentials(String login, String password, String key) {
        this.login = login;
        this.password = password;
        this.key = key;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }

    public String key() {
        return key;
    }
}
