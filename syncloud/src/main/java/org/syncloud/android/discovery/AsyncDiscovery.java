package org.syncloud.android.discovery;

import android.net.nsd.NsdManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.android.discovery.nsd.NsdDiscovery;
import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.discovery.Discovery;
import org.syncloud.discovery.JmdnsDiscovery;

public class AsyncDiscovery {

    private static Logger logger = LogManager.getLogger(AsyncDiscovery.class.getName());

    private WifiManager wifi;
    private WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = AsyncDiscovery.class.toString();
    private Discovery discovery;
    private DeviceEndpointListener deviceEndpointListener;
    private NsdManager manager;

    public AsyncDiscovery(WifiManager wifi, NsdManager manager, DeviceEndpointListener deviceEndpointListener) {
        this.wifi = wifi;
        this.deviceEndpointListener = deviceEndpointListener;
        this.manager = manager;
    }

    public void start(final String discoveryLibrary) {
        if (discovery == null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("creating multicast lock");
                        lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                        lock.setReferenceCounted(false);
                        lock.acquire();
                        if ("Android NSD".equals(discoveryLibrary)) {
                            discovery = new NsdDiscovery(manager, deviceEndpointListener, "syncloud");
                        } else {
                            WifiInfo connInfo = wifi.getConnectionInfo();
                            int ipAddress = connInfo.getIpAddress();
                            discovery = new JmdnsDiscovery(ipAddress, deviceEndpointListener, "syncloud");
                        }
                    } catch (Exception e) {
                        logger.error("failed to acquire multicast lock", e);
                    }

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
                        logger.error("failed to stop discovery");
                    } finally {
                        if (lock != null)
                            try {
                                lock.release();
                            } catch (Exception e) {
                                logger.error("failed to release multicast lock", e);
                            }
                        lock = null;
                    }
                }
            });
        }
    }

}
