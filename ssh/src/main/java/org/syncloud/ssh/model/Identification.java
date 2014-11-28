package org.syncloud.ssh.model;

import java.io.Serializable;

public class Identification implements Serializable {
    public String name;
    public String title;
    public String mac_address;

    public Identification() {}

    public Identification(String mac_address, String name, String title) {
        this.name = name;
        this.title = title;
        this.mac_address = mac_address;
    }
}
