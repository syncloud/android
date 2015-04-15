package org.syncloud.android.discovery;

import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.android.discovery.nsd.NsdDiscovery;
import org.syncloud.android.discovery.jmdns.JmdnsDiscovery;
import org.syncloud.android.network.Network;

import java.net.InetAddress;

public class DiscoveryManager {

    private static Logger logger = Logger.getLogger(DiscoveryManager.class.getName());
    private final Network network;

    private MulticastLock lock;

    private Discovery discovery;
    private NsdManager manager;
    private Boolean canceled = false;

    public DiscoveryManager(WifiManager wifi, NsdManager manager) {
        lock = new MulticastLock(wifi);
        network = new Network(wifi);
        this.manager = manager;
    }

    public void run(final String discoveryLibrary, int timeoutSeconds, DeviceEndpointListener deviceEndpointListener) {
        canceled = false;
        logger.info("starting discovery");
        if (discovery == null) {
            lock.acquire();
            if ("Android NSD".equals(discoveryLibrary)) {
                discovery = new NsdDiscovery(manager, deviceEndpointListener, "syncloud");
            } else {
                Optional<InetAddress> ip = network.inetAddress();
                if (ip.isPresent()) {
                    discovery = new JmdnsDiscovery(ip.get(), deviceEndpointListener, "syncloud");
                } else {
                    logger.error("unable to get local ip");
                    return;
                }
            }
            discovery.start();
            try {
                logger.info("waiting for " + timeoutSeconds + " seconds");
                int count = 0;
                while(count < timeoutSeconds && !canceled) {
                    Thread.sleep(1000);
                    count++;
                }
            } catch (InterruptedException e) {
                logger.error("sleep interrupted", e);
            }
            stop();
        }
    }

    public void cancel() {
        canceled = true;
    }

    private void stop() {
        logger.info("stopping discovery");
        if (discovery != null) {
            try {
                discovery.stop();
                discovery = null;
            } catch (Exception e) {
                logger.error("failed to stop discovery", e);
            }
            lock.release();
        }
    }

}
