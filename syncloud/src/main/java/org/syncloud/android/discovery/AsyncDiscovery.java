package org.syncloud.android.discovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
//import org.syncloud.discovery.DeviceEndpointListener;
//import org.syncloud.discovery.Discovery;

public class AsyncDiscovery {

    /*private WifiManager wifi;
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
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(false);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    discovery.start(connInfo.getIpAddress());
                } catch (Exception e) {
                    e.printStackTrace();
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
                } finally {
                    if (lock != null)
                        lock.release();
                    lock = null;
                }
            }
        });
    }*/

}
