package org.syncloud.ssh.model;

public class ProxyEndpoint {
    private String userDomain;

    public ProxyEndpoint(String userDomain) {
        this.userDomain = userDomain;
    }

    public String getUserDomain() {
        return userDomain;
    }

}
