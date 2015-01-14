package org.syncloud.redirect.model;

import java.io.Serializable;

public class Service implements Serializable {
    public int local_port;
    public String name;
    public int port;
    public String protocol;
    public String type;
    public String url;
}
