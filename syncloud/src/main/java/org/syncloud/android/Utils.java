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
    public static Device toDevice(Domain domain, Key key) {
        Service ssh_service = domain.service("ssh");
        Endpoint local_endpoint = new Endpoint(domain.local_ip, ssh_service.local_port);
        Identification identification = new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        String keyValue = null;
        if (key != null)
            keyValue = key.key;
        Credentials credentials = new Credentials("root", "syncloud", keyValue);

        Device device = new Device(
            domain.device_mac_address,
            identification,
            domain.user_domain,
            local_endpoint,
            credentials);

        return device;
    }

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
            devices.add(toDevice(domain, c));
        }
        return devices;
    }

}
