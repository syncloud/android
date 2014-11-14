package org.syncloud.android.discovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.google.common.base.Optional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MulticastLock {
    private static Logger logger = LogManager.getLogger(MulticastLock.class.getName());

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
            lock.setReferenceCounted(false);
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

    public Optional<InetAddress> ip() {
        WifiInfo connInfo = wifi.getConnectionInfo();
        int ipAddress = connInfo.getIpAddress();
        byte[] ip = ByteBuffer.allocate(4).putInt(ipAddress).array();
        InetAddress myAddress;
        try {
            myAddress = InetAddress.getByAddress(ip);
            logger.debug("address: " + myAddress);
            return Optional.of(myAddress);
        } catch (UnknownHostException e) {
            logger.error("Failed to get address: " + e.toString());
            return Optional.absent();
        }
    }
}
