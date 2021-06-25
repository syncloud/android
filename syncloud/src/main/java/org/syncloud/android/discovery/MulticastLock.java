package org.syncloud.android.discovery;

import android.net.wifi.WifiManager;

import org.apache.log4j.Logger;

public class MulticastLock {
    private static Logger logger = Logger.getLogger(MulticastLock.class.getName());

    public final static String MULTICAST_LOCK_TAG = MulticastLock.class.toString();

    private WifiManager.MulticastLock lock;
    private WifiManager wifi;

    public MulticastLock(WifiManager wifi) {
        this.wifi = wifi;
    }

    public void acquire() {
        logger.info("creating multicast lock");
        try {
            lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
            lock.setReferenceCounted(true);
            lock.acquire();
        } catch (Exception e) {
            logger.error("failed to acquire multicast lock", e);
            release();
        }
    }

    public void release() {
        if (lock != null) {
            try {
                logger.info("releasing multicast lock");
                lock.release();
            } catch (Exception e) {
                logger.error("failed to release multicast lock", e);
            }
        }
        lock = null;
    }


}
