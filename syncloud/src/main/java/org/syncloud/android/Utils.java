package org.syncloud.android;

import org.syncloud.android.core.redirect.model.Domain;
import org.syncloud.android.core.platform.model.DomainModel;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Utils {
    public static List<DomainModel> toDevices(List<Domain> domains) {
        List<DomainModel> devices = newArrayList();
        for (Domain domain: domains) {
            if (hasDeviceInfo(domain)) {
                DomainModel domainModel = new DomainModel(domain);
                devices.add(domainModel);
            }
        }
        return devices;
    }

    private static boolean hasDeviceInfo(Domain domain) {
        return (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null);

    }
}
