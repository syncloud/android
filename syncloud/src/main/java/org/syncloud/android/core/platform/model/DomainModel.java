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

    public String userDomain() {
        return domain.user_domain;
    }

    public Identification id() {
        return id;
    }

    public String getDnsUrl(String mainDomain) {
        Integer port = domain.map_local_address ? domain.web_local_port : domain.web_port;
        if (port == null) return null;
        return getUrl(domain.web_protocol, format("%s.%s", domain.user_domain, mainDomain), port);
    }

    public String getExternalUrl() {
        if (domain.ip == null || domain.web_port == null)
            return null;
        return getUrl(domain.web_protocol, domain.ip, domain.web_port);
    }

    public String getInternalUrl() {
        if (domain.local_ip == null || domain.web_local_port == null)
            return null;
        return getUrl(domain.web_protocol, domain.local_ip, domain.web_local_port);
    }

    private static String getUrl(String protocol, String address, int port) {
        String url = format("%s://%s", protocol, address);
        if ((protocol.equals("http") && port != 80) ||
            (protocol.equals("https") && port != 443))
            url += ":"+ Integer.toString(port);
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
