package org.syncloud.common.upnp.cling;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.syncloud.common.upnp.UPnP;

public class ClingUPnP implements UPnP {

    public static final int TIMEOUT = 60;
    private static Logger logger = Logger.getLogger(ClingUPnP.class);
    private boolean started = false;
    private boolean used = false;
    private UpnpService upnpService;
    private final RouterListener routerListener;
    private UpnpServiceConfiguration configuration;

    public ClingUPnP(UpnpServiceConfiguration configuration) {
        this.configuration = configuration;
        routerListener = new RouterListener(TIMEOUT);
    }

    public synchronized void start() {
        if (used)
            throw new RuntimeException("cannot use me two times");
        logger.info("starting upnp service");
        upnpService = new UpnpServiceImpl(configuration, routerListener);
        started = true;
        used = true;
    }


    @Override
    public Optional<ClingRouter> find() {
        if (!started) {
            logger.error("not started");
            return Optional.absent();
        }
        upnpService.getControlPoint().search(TIMEOUT);
        return routerListener.await();
    }

    public synchronized void shutdown() {
        if (started)
            upnpService.shutdown();
        started = false;
    }
}
