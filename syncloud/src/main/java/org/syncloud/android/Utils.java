package org.syncloud.android;

import com.google.common.base.Optional;

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
    private static Optional<Credentials> find(List<Key> keys, String mac_address) {
        for (Key key: keys)
            if (key.macAddress.equals(mac_address))
                return Optional.of(key.credentials);
        return Optional.absent();
    }

    public static List<DomainModel> toDevices(List<Domain> domains, List<Key> keys) {
        List<DomainModel> devices = newArrayList();
        for (Domain domain: domains) {
            Optional<Credentials> credentials = find(keys, domain.device_mac_address);

            Service rest = domain.service("server");
            Identification id = deviceId(domain);

            if (domain.local_ip != null && domain.ip != null && id != null && rest != null) {
                Endpoint localEndpoint = new Endpoint(domain.local_ip, rest.local_port);
                Endpoint remoteEndpoint = new Endpoint(domain.ip, rest.port);
                if (credentials.isPresent()) {
                    Device device = new Device(id, localEndpoint, remoteEndpoint, credentials.get());
                    DomainModel model = new DomainModel(domain.user_domain, device);
                    devices.add(model);
                }
            }


        }
        return devices;
    }

    private static Identification deviceId(Domain domain) {
        if (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null)
            return new Identification(domain.device_mac_address, domain.device_name, domain.device_title);
        return null;
    }

}
