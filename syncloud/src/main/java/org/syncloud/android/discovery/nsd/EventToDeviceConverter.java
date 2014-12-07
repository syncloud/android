package org.syncloud.android.discovery.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class EventToDeviceConverter implements NsdManager.DiscoveryListener {

    private static Logger logger = Logger.getLogger(EventToDeviceConverter.class.getName());

    private NsdManager manager;
    private String lookForServiceName;

    private Resolver resolver;

    private List<String> discoveredServices = newArrayList();

    public EventToDeviceConverter(NsdManager manager, String lookForServiceName, Resolver resolver) {
        this.manager = manager;
        this.lookForServiceName = lookForServiceName.toLowerCase();
        this.resolver = resolver;
    }

    @Override
    public void onStartDiscoveryFailed(String s, int i) {
        String text = "start discovery failed "+s;
        logger.error(text);
        manager.stopServiceDiscovery(this);
    }

    @Override
    public void onStopDiscoveryFailed(String s, int i) {
        String text = "stop discovery failed "+s;
        logger.error(text);
        manager.stopServiceDiscovery(this);
    }

    @Override
    public void onDiscoveryStarted(String s) {
        String text = "discovery started "+s;
        logger.info(text);
    }

    @Override
    public void onDiscoveryStopped(String s) {
        String text = "discovery stopped "+s;
        logger.info(text);
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        String serviceName = serviceInfo.getServiceName().toLowerCase();
        String text = "service found "+serviceName;
        logger.info(text);
        if (!discoveredServices.contains(serviceName)) {
            if (serviceName.contains(lookForServiceName)) {
                discoveredServices.add(serviceName);
                text = "starting resolving service " + serviceName;
                logger.info(text);
                resolver.resolve(serviceInfo);
            }
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        String text = "service lost "+serviceInfo.getServiceName();
        logger.info(text);
    }
}
