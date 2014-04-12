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
    private String url = null;
    private String serviceName;

    public EventListener(String serviceName) {
        this.serviceName = serviceName;
    }

    public Optional<String> getUrl() {
        try {
            latch.await(30, TimeUnit.SECONDS);
            return Optional.fromNullable(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        logger.debug("add: " + event.getType());
        if (event.getName().equals(serviceName)) {
            logger.debug("add: " + event.getInfo().getPort());
            logger.debug("add: " + event.getInfo().getServer());

            ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());

            logger.debug("more: " + info.getType());
//            logger.debug("more: " + info.getInet4Addresses()[0].getHostAddress());

            String server = info.getServer();
            String local = ".local.";
            if (server.endsWith(local))
                server = server.substring(0, server.length() - local.length());

            url = "http://" + server + ":" + info.getPort() + info.getPropertyString("path");
            latch.countDown();
        }
    }

    @Override
    public void serviceRemoved(ServiceEvent event) { }
    @Override
    public void serviceResolved(ServiceEvent event) { }

}
