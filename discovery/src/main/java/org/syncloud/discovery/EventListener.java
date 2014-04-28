package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class EventListener implements ServiceListener {


    private static Logger logger = LogManager.getLogger(EventListener.class.getName());

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private List<String> urls = new ArrayList<String>();
    private String serviceName;

    public EventListener(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getUrl() {
        try {

            lock.lock();

            while (condition.await(2, TimeUnit.SECONDS)) {
                logger.debug("will wait for another one");
            }

            lock.unlock();

        } catch (InterruptedException e) {
            logger.error("interrupted", e);
        }
        return urls;
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        String eventName = event.getName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), eventName);
            waitForIpv4(info);
            urls.add(extractUrl(info));
            condition.signal();
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
