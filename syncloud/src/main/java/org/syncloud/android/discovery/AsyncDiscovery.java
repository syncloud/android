package org.syncloud.android.discovery;

import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.android.discovery.nsd.NsdDiscovery;
import org.syncloud.android.discovery.jmdns.JmdnsDiscovery;

public class AsyncDiscovery {

    private static Logger logger = LogManager.getLogger(AsyncDiscovery.class.getName());

    MulticastLock lock;

    private Discovery discovery;
    private DeviceEndpointListener deviceEndpointListener;
    private NsdManager manager;

    public AsyncDiscovery(WifiManager wifi, NsdManager manager, DeviceEndpointListener deviceEndpointListener) {
        this.lock = new MulticastLock(wifi);
        this.deviceEndpointListener = deviceEndpointListener;
        this.manager = manager;
    }

    public void start(final String discoveryLibrary) {
        if (discovery == null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    lock.acquire();
                    if ("Android NSD".equals(discoveryLibrary)) {
                        discovery = new NsdDiscovery(manager, deviceEndpointListener, "syncloud");
                    } else {
                        if (lock.ip().isPresent()) {
                            discovery = new JmdnsDiscovery(lock.ip().get(), deviceEndpointListener, "syncloud");
                        } else {
                            logger.error("unable to get local ip");
                            return;
                        }
                    }
                    discovery.start();
                }
            });
        }
    }

    public void stop() {
        if (discovery != null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        discovery.stop();
                        discovery = null;
                    } catch (Exception e) {
                        logger.error("failed to stop discovery", e);
                    }
                    lock.release();
                }
            });
        }
    }

}
