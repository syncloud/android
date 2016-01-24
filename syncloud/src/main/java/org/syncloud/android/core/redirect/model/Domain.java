package org.syncloud.android.core.redirect.model;

import java.io.Serializable;
import java.util.List;

public class Domain implements Serializable {
    public String user_domain;
    public String device_mac_address;
    public String device_name;
    public String device_title;
    public Boolean map_local_address;
    public String web_protocol;
    public Integer web_local_port;
    public Integer web_port;
    public String ip;
    public String local_ip;
    public String last_update;
}
