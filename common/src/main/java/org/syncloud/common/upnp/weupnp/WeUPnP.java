package org.syncloud.common.upnp.weupnp;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
//import org.bitlet.weupnp.GatewayDiscover;
import org.syncloud.common.upnp.UPnP;

import java.net.InetAddress;
import java.util.Map;

public class WeUPnP implements UPnP {

    private Logger logger = Logger.getLogger(WeUPnP.class);

    @Override
    public Optional<WeRouter> find() {
        GatewayDiscover discover = new GatewayDiscover();
        try {
            discover.discover();
//            Map<InetAddress, GatewayDevice> allGateways = discover.getAllGateways();
            GatewayDevice device = discover.getValidGateway();
            if (device != null)
                return Optional.of(new WeRouter(device));
        } catch (Exception e) {
            logger.error("unable to find upnp router, " + e.getMessage());
        }
        return Optional.absent();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
