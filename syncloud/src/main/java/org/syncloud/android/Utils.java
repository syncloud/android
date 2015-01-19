package org.syncloud.android;

import org.syncloud.redirect.model.Domain;
import org.syncloud.ssh.model.Device;
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
            devices.add(new Device(domain, c));
        }
        return devices;
    }

}
