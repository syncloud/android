package org.syncloud.discovery;

import com.google.common.base.Optional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class EventListener implements ServiceListener {


    private static Logger logger = LogManager.getLogger(EventListener.class.getName());

    private CountDownLatch latch = new CountDownLatch(1);
    private CountDownLatch discoveryLatch = new CountDownLatch(1);
    private String url = null;
    private String serviceName;

    public EventListener(String serviceName) {
        this.serviceName = serviceName;
    }

    public Optional<String> getUrl() {
        try {
            discoveryLatch.await(30, TimeUnit.SECONDS);
            return Optional.fromNullable(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        if (event.getName().equals(serviceName)) {
            ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());
            waitForIpv4(info);
            url = extractUrl(info);
            discoveryLatch.countDown();
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

    @Override
    public void serviceRemoved(ServiceEvent event) { }
    @Override
    public void serviceResolved(ServiceEvent event) { }

}
