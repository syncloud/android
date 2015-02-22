package org.syncloud.common.upnp;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.syncloud.common.upnp.weupnp.GatewayDiscover;

public class UPnP {

    private Logger logger = Logger.getLogger(UPnP.class);

    static {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }

    public Optional<Router> find() {
        GatewayDiscover discover = new GatewayDiscover();
        try {
            discover.discover();
            GatewayDevice device = discover.getValidGateway();
            if (device != null)
                return Optional.of(new Router(device));
        } catch (Exception e) {
            logger.error("unable to find upnp router, " + e.getMessage());
        }
        return Optional.absent();
    }
}
