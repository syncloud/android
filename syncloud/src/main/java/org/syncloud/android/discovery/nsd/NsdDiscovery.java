package org.syncloud.android.discovery.nsd;

import android.net.nsd.NsdManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.discovery.Discovery;
import org.syncloud.discovery.JmdnsDiscovery;

public class NsdDiscovery implements Discovery {

    private static Logger logger = LogManager.getLogger(JmdnsDiscovery.class.getName());

    public static final String TYPE = "_ssh._tcp.";

    private NsdManager manager;
    private NsdManager.DiscoveryListener listener;
    private boolean started = false;

    public NsdDiscovery(NsdManager manager, DeviceEndpointListener deviceEndpointListener, String serviceName) {
        this.manager = manager;
        this.listener = new EventToDeviceConverter(manager, serviceName, deviceEndpointListener);
    }

    public void start() {
        logger.info("starting discovery");
        if (started){
            logger.error("already started, stop first");
            return;
        }

        try {
            logger.info("starting discovery with listener");
            manager.discoverServices(TYPE, NsdManager.PROTOCOL_DNS_SD, this.listener);
            started = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public void stop() {
        logger.info("stopping discovery");

        if (!started) {
            logger.error("discovery not started, start first");
            return;
        }

        try {
            started = false;
            logger.info("stopping discovery with listener");
            manager.stopServiceDiscovery(this.listener);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
}