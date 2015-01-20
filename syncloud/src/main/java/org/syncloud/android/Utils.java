package org.syncloud.android;

import org.syncloud.redirect.model.Domain;
import org.syncloud.redirect.model.Service;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;
import org.syncloud.ssh.model.Key;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Utils {
    private static Key find(List<Key> keys, String mac_address) {
        for (Key key: keys)
            if (key.macAddress.equals(mac_address))
                return key;
        return null;
    }

    public static List<Device> toDevices(List<Domain> domains, List<Key> keys) {
        List<Device> devices = newArrayList();
        for (Domain domain: domains) {
            Key c = find(keys, domain.device_mac_address);
            Service sshService = domain.service("ssh");
            //TODO: We actually need to show these devices
            if (sshService != null) {
                devices.add(create(sshService, domain, c));
            }
        }
        return devices;
    }

    private static Device create(Service sshService, Domain domain, Key key) {

        Endpoint localEndpoint = new Endpoint(domain.local_ip, sshService.local_port);
        Endpoint remoteEndpoint = new Endpoint(domain.ip, sshService.port);
        Identification identification = new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        String keyValue = null;
        if (key != null)
            keyValue = key.key;
        Credentials credentials = new Credentials("root", "syncloud", keyValue);

        return new Device(
                identification,
                domain.user_domain,
                localEndpoint,
                remoteEndpoint,
                credentials);
    }

}
