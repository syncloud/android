package org.syncloud.integration.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.model.Device;

import java.util.HashMap;
import java.util.Map;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class EventToDeviceConverter implements ServiceListener {

    private static Logger logger = LogManager.getLogger(EventToDeviceConverter.class.getName());

    private String serviceName;
    private DeviceListener deviceListener;
    private Map<String, Device> serviceToUrl = new HashMap<String, Device>();

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

            Device device = extractDevice(info);
            serviceToUrl.put(eventName, device);
            if (deviceListener != null)
                deviceListener.added(device);

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


    private Device extractDevice(ServiceInfo info) {
        String address = "unknown";
        if (info.getInet4Addresses().length > 0) {
            address = info.getInet4Addresses()[0].getHostAddress();
        } else {
            String server = info.getServer();
            String local = ".local.";
            if (server.endsWith(local))
                address = server.substring(0, server.length() - local.length());
        }

        Device device = new Device(address, info.getPort(), info.getPropertyString("path"));

        logger.debug(device.getOwnCloudUrl());

        return device;
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        logger.debug("service removed name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
        String eventName = event.getName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            Device device = serviceToUrl.remove(eventName);
            if (deviceListener != null)
                deviceListener.removed(device);
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        //TODO: Not using this as sometime it is not even called
    }

}
