package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class EventListener implements ServiceListener {

    private static Logger logger = LogManager.getLogger(EventListener.class.getName());

    private String serviceName;
    private DeviceLisener deviceLisener;

    public EventListener(String serviceName) {
        this.serviceName = serviceName;
    }

    public EventListener(String serviceName, DeviceLisener deviceLisener) {
        this.serviceName = serviceName;
        this.deviceLisener = deviceLisener;
    }


    @Override
    public void serviceAdded(final ServiceEvent event) {

        String eventName = event.getName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            logger.debug("service added name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
            event.getDNS().getServiceInfo(event.getType(), eventName);
        }
    }

    private String extractUrl(ServiceInfo info) {
        String address = "unknown";
        if (info.getInet4Addresses().length > 0) {
            address = info.getInet4Addresses()[0].getHostAddress();
        } else {
            String server = info.getServer();
            String local = ".local.";
            if (server.endsWith(local))
                address = server.substring(0, server.length() - local.length());
        }
        String url = "http://" + address + ":" + info.getPort() + info.getPropertyString("path");

        logger.debug(url);

        return url;
    }

    @Override
    public void serviceRemoved(ServiceEvent event) { }

    @Override
    public void serviceResolved(ServiceEvent event) {
        String eventName = event.getName();
        logger.debug("service resolved name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            String url = extractUrl(event.getInfo());
            if (deviceLisener != null)
                deviceLisener.added(url);
        }
    }

}
