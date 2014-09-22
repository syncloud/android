package org.syncloud.android.discovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.google.common.eventbus.EventBus;

import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.discovery.Discovery;

public class AsyncDiscovery {

    private WifiManager wifi;
    private WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = AsyncDiscovery.class.toString();
    private Discovery discovery;
    public EventBus eventBus;

    public AsyncDiscovery(WifiManager wifi, DeviceEndpointListener deviceEndpointListener, EventBus eventBus) {
        this.wifi = wifi;
        this.eventBus = eventBus;
        discovery = new Discovery(deviceEndpointListener, "syncloud");
    }

    public void start() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    eventBus.post(new Event("locked"));
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(false);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    discovery.start(connInfo.getIpAddress());
                    eventBus.post(new Event("started"));
                } catch (Exception e) {
                    e.printStackTrace();
                    eventBus.post(new Event("start error"));
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
                    eventBus.post(new Event("stopped"));
                } catch (Exception e) {
                    e.printStackTrace();
                    eventBus.post(new Event("stop error"));
                } finally {
                    if (lock != null) {
                        lock.release();
                        eventBus.post(new Event("released"));
                        eventBus.post(new Event(lock.isHeld() ? "held" : "not held"));
                    }
                    lock = null;
                }
            }
        });
    }

}

