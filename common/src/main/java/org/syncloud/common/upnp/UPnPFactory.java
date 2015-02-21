package org.syncloud.common.upnp;

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.syncloud.common.upnp.cling.ClingUPnP;
import org.syncloud.common.upnp.weupnp.WeUPnP;

public class UPnPFactory {
    public enum TYPE {CLING, WEUPNP}
    public static UPnP createUPnP(TYPE type) {
        switch (type) {
            case CLING:
                return new ClingUPnP(new AndroidUpnpServiceConfiguration());
            case WEUPNP:
            default:
                return new WeUPnP();
        }
    }
}
