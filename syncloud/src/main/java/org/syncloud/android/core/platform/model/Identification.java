package org.syncloud.android.core.platform.model;

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

    public String macAddress() { return mac_address; }

    public String name() { return name; }

    public String title() { return title; }

    @Override
    public String toString() {
        return "Identification{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", mac_address='" + mac_address + '\'' +
                '}';
    }
}
