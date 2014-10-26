package org.syncloud.android.discovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.discovery.Discovery;

public class AsyncDiscovery {

    private static Logger logger = LogManager.getLogger(AsyncDiscovery.class.getName());

    private WifiManager wifi;
    private WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = AsyncDiscovery.class.toString();
    private Discovery discovery;

    public AsyncDiscovery(WifiManager wifi, DeviceEndpointListener deviceEndpointListener) {
        this.wifi = wifi;
        discovery = new Discovery(deviceEndpointListener, "syncloud");

    }

    public void start() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("creating multicast lock");
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(false);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    discovery.start(connInfo.getIpAddress());
                } catch (Exception e) {
                    logger.error("failed to acquire multicast lock", e);
                }

            }
        });
    }

    public void stop() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    discovery.stop();
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
