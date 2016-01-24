package org.syncloud.android;

import org.syncloud.android.core.redirect.model.Domain;
import org.syncloud.android.core.platform.model.DomainModel;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Utils {
    public static List<DomainModel> toModels(List<Domain> domains) {
        List<DomainModel> models = newArrayList();
        for (Domain domain: domains) {
            if (hasDeviceInfo(domain)) {
                DomainModel domainModel = new DomainModel(domain);
                models.add(domainModel);
            }
        }
        return models;
    }

    private static boolean hasDeviceInfo(Domain domain) {
        return (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null);

    }
}
