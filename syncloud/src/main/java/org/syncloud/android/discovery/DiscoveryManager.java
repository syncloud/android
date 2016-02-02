package org.syncloud.android.discovery;

import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;

import org.apache.log4j.Logger;
import org.syncloud.android.discovery.nsd.NsdDiscovery;

public class DiscoveryManager {

    private static Logger logger = Logger.getLogger(DiscoveryManager.class.getName());

    private MulticastLock lock;

    private Discovery discovery;
    private NsdManager manager;
    private Boolean canceled = false;

    public static int ACTIVATION_PORT = 81;

    public DiscoveryManager(WifiManager wifi, NsdManager manager) {
        lock = new MulticastLock(wifi);
        this.manager = manager;
    }

    public void run(int timeoutSeconds, DeviceEndpointListener deviceEndpointListener) {
        canceled = false;
        logger.info("starting discovery");
        if (discovery == null) {
            lock.acquire();
            discovery = new NsdDiscovery(manager, deviceEndpointListener, "syncloud");
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
