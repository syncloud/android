package org.syncloud.android.discovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import org.syncloud.discovery.DeviceListener;
import org.syncloud.discovery.Discovery;

public class AsyncDiscovery {

    private WifiManager wifi;
    private WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = AsyncDiscovery.class.toString();
    private Discovery discovery;

    public AsyncDiscovery(WifiManager wifi, DeviceListener deviceListener) {
        this.wifi = wifi;
        discovery = new Discovery(deviceListener, "ownCloud");

    }

    public void start() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(true);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    discovery.start(connInfo.getIpAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.release();
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
                    e.printStackTrace();
                }
            }
        });
    }

}
