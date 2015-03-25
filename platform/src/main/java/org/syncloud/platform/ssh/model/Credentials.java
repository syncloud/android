package org.syncloud.platform.ssh.model;

import java.io.Serializable;

public class Credentials implements Serializable {

    public static Credentials getStandardCredentials() {
        return new Credentials("root", "syncloud", null);
    }

    public String login;
    public String password;
    public String key;

    //JSON
    public Credentials() {}

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
