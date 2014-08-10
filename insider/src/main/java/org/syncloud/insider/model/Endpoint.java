package org.syncloud.insider.model;

public class Endpoint {
    private Service service;
    private String external_host;
    private int external_port;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getExternal_host() {
        return external_host;
    }

    public void setExternal_host(String external_host) {
        this.external_host = external_host;
    }

    public int getExternal_port() {
        return external_port;
    }

    public void setExternal_port(int external_port) {
        this.external_port = external_port;
    }
}
