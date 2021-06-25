package org.syncloud.android.core.platform.model;

import org.syncloud.android.core.redirect.model.Domain;

import java.io.Serializable;

import static java.lang.String.format;

public class DomainModel implements Serializable {
    private Domain domain;
    private Identification id;

    private static Identification deviceId(Domain domain) {
        if (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null)
            return new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        return null;
    }

    public DomainModel(Domain domain) {
        this.domain  = domain;
        this.id = deviceId(domain);
    }

    public String name() {
        return domain.name;
    }

    public Identification id() {
        return id;
    }

    public String getDnsUrl() {
        Integer port = domain.map_local_address ? domain.web_local_port : domain.web_port;
        String url = format("%s://%s", domain.web_protocol, domain.name);
        if ((domain.web_protocol.equals("http") && port != 80) ||
            (domain.web_protocol.equals("https") && port != 443))
            url += ":" + (int) port;
        return url;
    }

    @Override
    public String toString() {
        return "DomainModel{" +
                "domain=" + domain +
                ", id=" + id +
                '}';
    }
}
