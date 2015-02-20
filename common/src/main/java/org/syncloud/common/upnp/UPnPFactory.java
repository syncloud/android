package org.syncloud.common.upnp;

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.syncloud.common.upnp.cling.ClingUPnP;

public class UPnPFactory {
    public static UPnP createUPnP(String name) {
        return new ClingUPnP(new AndroidUpnpServiceConfiguration());
    }
}
