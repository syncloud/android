package org.syncloud.platform.ssh.model;

import java.io.Serializable;

public class Credentials implements Serializable {

    public String login;
    public String password;

    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }

}
