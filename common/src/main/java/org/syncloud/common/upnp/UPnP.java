package org.syncloud.common.upnp;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.syncloud.common.upnp.igd.Router;
import org.syncloud.common.upnp.igd.RouterListener;

public class UPnP {

    private static Logger logger = Logger.getLogger(UPnP.class);
    private boolean started = false;
    private UpnpService upnpService;
    private final RouterListener routerListener;

    public UPnP() {
        routerListener = new RouterListener();
    }

    public synchronized void start(UpnpServiceConfiguration configuration) {
        logger.info("starting upnp service");
        upnpService = new UpnpServiceImpl(configuration, routerListener);
        started = true;
    }


    public Optional<Router> find(int seconds) {
        if (!started) {
            logger.error("not started");
            return Optional.absent();
        }
        routerListener.reset();
        upnpService.getControlPoint().search(seconds);
        return routerListener.await(seconds);
    }

    public synchronized void shutdown() {
        if (started)
            upnpService.shutdown();
        started = false;
    }
}
