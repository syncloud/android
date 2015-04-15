package org.syncloud.redirect.model;

import java.io.Serializable;
import java.util.List;

public class Domain implements Serializable {
    public String user_domain;
    public String device_mac_address;
    public String device_name;
    public String device_title;
    public String ip;
    public String local_ip;
    public String last_update;
    public List<Service> services;

    public Service service(String name) {
        for (Service s: services)
            if (s.name.equals(name))
                return s;
        return null;
    }
}
