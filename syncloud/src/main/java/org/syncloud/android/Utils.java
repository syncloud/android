package org.syncloud.android;

import org.syncloud.redirect.model.Domain;
import org.syncloud.redirect.model.Service;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.Device;
import org.syncloud.platform.ssh.model.DomainModel;
import org.syncloud.platform.ssh.model.Endpoint;
import org.syncloud.platform.ssh.model.Identification;
import org.syncloud.platform.ssh.model.Key;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Utils {
    private static Key find(List<Key> keys, String mac_address) {
        for (Key key: keys)
            if (key.macAddress.equals(mac_address))
                return key;
        return null;
    }

    public static List<DomainModel> toDevices(List<Domain> domains, List<Key> keys) {
        List<DomainModel> devices = newArrayList();
        for (Domain domain: domains) {
            Key c = find(keys, domain.device_mac_address);
            devices.add(create(domain, c));
        }
        return devices;
    }

    private static DomainModel create(Domain domain, Key key) {
        Device device = null;

        Service sshService = domain.service("ssh");
        Identification id = deviceId(domain);

        if (domain.local_ip != null && domain.ip != null && id != null && sshService != null) {
            Endpoint localEndpoint = new Endpoint(domain.local_ip, sshService.local_port);
            Endpoint remoteEndpoint = new Endpoint(domain.ip, sshService.port);
            String keyValue = null;
            if (key != null)
                keyValue = key.key;
            Credentials credentials = new Credentials("root", "syncloud", keyValue);

            device = new Device(id, localEndpoint, remoteEndpoint, credentials);
        }

        return new DomainModel(domain.user_domain, device);
    }

    private static Identification deviceId(Domain domain) {
        if (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null)
            return new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        return null;
    }

}
