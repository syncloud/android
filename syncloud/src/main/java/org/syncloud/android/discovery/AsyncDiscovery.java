package org.syncloud.android.discovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.discovery.Discovery;

import java.util.ArrayList;
import java.util.List;

public class AsyncDiscovery {

    private WifiManager wifi;
    private WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = AsyncDiscovery.class.toString();
    private Discovery discovery;
    public List<Event> events;

    public AsyncDiscovery(WifiManager wifi, DeviceEndpointListener deviceEndpointListener, List<Event> events) {
        this.wifi = wifi;
        this.events = events;
        discovery = new Discovery(deviceEndpointListener, "syncloud");
    }

    public void start() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    events.add(new Event("locked"));
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(false);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    discovery.start(connInfo.getIpAddress());
                    events.add(new Event("started"));
                } catch (Exception e) {
                    e.printStackTrace();
                    events.add(new Event("start error"));
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
                    events.add(new Event("stopped"));
                } catch (Exception e) {
                    e.printStackTrace();
                    events.add(new Event("stop error"));
                } finally {
                    if (lock != null) {
                        lock.release();
                        events.add(new Event("released"));
                        events.add(new Event(lock.isHeld() ? "held" : "not held"));
                    }
                    lock = null;
                }
            }
        });
    }

}

