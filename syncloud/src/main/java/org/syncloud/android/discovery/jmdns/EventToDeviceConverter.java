package org.syncloud.android.discovery.jmdns;

import org.apache.log4j.Logger;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.android.discovery.DiscoveryManager;
import org.syncloud.platform.ssh.model.Endpoint;

import java.util.HashMap;
import java.util.Map;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class EventToDeviceConverter implements ServiceListener {

    private static Logger logger = Logger.getLogger(EventToDeviceConverter.class.getName());

    private String serviceName;
    private DeviceEndpointListener deviceEndpointListener;
    private Map<String, Endpoint> serviceToUrl = new HashMap<String, Endpoint>();

    public EventToDeviceConverter(String serviceName, DeviceEndpointListener deviceEndpointListener) {
        this.serviceName = serviceName;
        this.deviceEndpointListener = deviceEndpointListener;
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {

        String eventName = event.getName();
        logger.debug("service added: " + event);

        if (!serviceToUrl.containsKey(eventName)) {
            if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
                logger.debug("service added name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
                ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), eventName);
                waitForIpv4(info);

                Endpoint device = extractDevice(info);
                logger.info(device);
                serviceToUrl.put(eventName, device);
                if (deviceEndpointListener != null)
                    deviceEndpointListener.added(device);

            }
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
                logger.error("interrupted", e);
            }
            retry++;
        }
    }


    private Endpoint extractDevice(ServiceInfo info) {
        logger.debug("extracting device: " + info);

        String address = "unknown";
        if (info.getInet4Addresses().length > 0) {
            address = info.getInet4Addresses()[0].getHostAddress();
        } else {
            String server = info.getServer();
            String local = ".local.";
            if (server.endsWith(local))
                address = server.substring(0, server.length() - local.length());
        }

        return new Endpoint(address, DiscoveryManager.ACTIVATION_PORT);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        logger.debug("service removed name: " + event.getName() + ", ip4 addresses: " + event.getInfo().getInet4Addresses().length);
        String eventName = event.getName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            Endpoint device = serviceToUrl.remove(eventName);
            if (deviceEndpointListener != null)
                deviceEndpointListener.removed(device);
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        logger.debug("not used event: " + event);
        //TODO: Not using this as sometime it is not even called
    }

}
