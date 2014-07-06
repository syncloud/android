package org.syncloud.model;

public class User {
    private Boolean active;
    private String email;
    private String ip;
    private Integer port;
    private String update_token;
    private String user_domain;


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUpdate_token() {
        return update_token;
    }

    public void setUpdate_token(String update_token) {
        this.update_token = update_token;
    }

    public String getUser_domain() {
        return user_domain;
    }

    public void setUser_domain(String user_domain) {
        this.user_domain = user_domain;
    }
}
