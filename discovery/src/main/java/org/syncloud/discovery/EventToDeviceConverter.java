package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class EventToDeviceConverter implements ServiceListener {

    private static Logger logger = LogManager.getLogger(EventToDeviceConverter.class.getName());

    private String serviceName;
    private DeviceListener deviceListener;
    private Map<String, String> serviceToUrl = new HashMap<String, String>();

    public EventToDeviceConverter(String serviceName, DeviceListener deviceListener) {
        this.serviceName = serviceName;
        this.deviceListener = deviceListener;
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {

        String eventName = event.getName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            logger.debug("service added name: " + event.getName()+ ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
            ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), eventName);
            waitForIpv4(info);

            String url = extractUrl(info);
            serviceToUrl.put(eventName, url);
            if (deviceListener != null)
                deviceListener.added(url);

        }
    }

    private void waitForIpv4(ServiceInfo info) {
        //TODO: Looks like info is updated asynchronously and ipv4 arrives a bit later
        int retry = 0;
        while (info.getInet4Addresses().length == 0 && retry < 10) {
            try {
                logger.debug("waiting for ipv4");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry++;
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
    public void serviceRemoved(ServiceEvent event) {
        logger.debug("service removed name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
        String eventName = event.getName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            String url = serviceToUrl.remove(eventName);
            if (deviceListener != null)
                deviceListener.removed(url);
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        String eventName = event.getName();
        logger.debug("service resolved name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
        /*if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            String url = extractUrl(event.getInfo());
            if (deviceListener != null)
                deviceListener.added(url);
        }*/
    }

}
