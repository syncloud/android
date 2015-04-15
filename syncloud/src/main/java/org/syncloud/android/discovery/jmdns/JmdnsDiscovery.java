package org.syncloud.android.discovery.jmdns;

import org.apache.log4j.Logger;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.android.discovery.Discovery;

import java.net.InetAddress;

import javax.jmdns.JmDNS;

public class JmdnsDiscovery implements Discovery {

    private static Logger logger = Logger.getLogger(JmdnsDiscovery.class);

    public static final String TYPE = "_ssh._tcp.local.";
    private JmDNS jmdns;
    private EventToDeviceConverter listener;
    private boolean started = false;
    private InetAddress ip;

    public JmdnsDiscovery(InetAddress ip, DeviceEndpointListener deviceEndpointListener, String serviceName) {
        this.ip = ip;
        listener = new EventToDeviceConverter(serviceName, deviceEndpointListener);
    }

    public void start() {

        if (started){
            logger.error("already started, stop first");
            return;
        }

        try {
            logger.info("creating jmdns");
            jmdns = JmDNS.create(ip);
            jmdns.addServiceListener(TYPE, listener);
            started = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stop() {

        if (!started) {
            logger.error("not started, start first");
            return;
        }

        try {
            jmdns.removeServiceListener(TYPE, listener);
            logger.info("closing jmdns");
            jmdns.close();
            logger.info("closing jmdns: done");
            started = false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}