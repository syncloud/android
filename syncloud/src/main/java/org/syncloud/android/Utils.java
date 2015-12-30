package org.syncloud.android;

import com.google.common.base.Optional;

import org.syncloud.android.core.redirect.model.Domain;
import org.syncloud.android.core.redirect.model.Service;
import org.syncloud.android.core.platform.model.Credentials;
import org.syncloud.android.core.platform.model.Device;
import org.syncloud.android.core.platform.model.DomainModel;
import org.syncloud.android.core.platform.model.Endpoint;
import org.syncloud.android.core.platform.model.Identification;
import org.syncloud.android.core.platform.model.Key;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Utils {
    private static Optional<Credentials> find(List<Key> keys, String mac_address) {
        for (Key key: keys)
            if (key.macAddress.equals(mac_address))
                return Optional.of(key.credentials);
        return Optional.absent();
    }

    public static List<DomainModel> toDevices(List<Domain> domains) {
        List<DomainModel> devices = newArrayList();
        for (Domain domain: domains) {
            Service rest = domain.service("server");
            Identification id = deviceId(domain);

            if (domain.local_ip != null && domain.ip != null && id != null && rest != null) {
                Endpoint localEndpoint = new Endpoint(domain.local_ip, rest.local_port);
                Endpoint remoteEndpoint = new Endpoint(domain.ip, rest.port);
                Device device = new Device(id, localEndpoint, remoteEndpoint);
                DomainModel model = new DomainModel(domain.user_domain, device);
                devices.add(model);
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
