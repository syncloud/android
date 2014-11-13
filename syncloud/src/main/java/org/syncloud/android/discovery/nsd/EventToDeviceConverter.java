package org.syncloud.android.discovery.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.DirectEndpoint;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class EventToDeviceConverter implements NsdManager.DiscoveryListener {

    private static Logger logger = LogManager.getLogger(EventToDeviceConverter.class.getName());

    private NsdManager manager;
    private String serviceName;
    private DeviceEndpointListener deviceEndpointListener;
    private Map<String, DirectEndpoint> serviceToUrl = new HashMap<String, DirectEndpoint>();

    public EventToDeviceConverter(NsdManager manager, String serviceName, DeviceEndpointListener deviceEndpointListener) {
        this.manager = manager;
        this.serviceName = serviceName;
        this.deviceEndpointListener = deviceEndpointListener;
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
        String text = "service found "+serviceInfo.getServiceName();
        logger.info(text);
        if (!serviceToUrl.containsKey(serviceInfo.getServiceName())) {
            if (serviceInfo.getServiceName().toLowerCase().contains(serviceName.toLowerCase())) {
                text = "starting resolving service " + serviceInfo.getServiceName();
                logger.info(text);
                manager.resolveService(serviceInfo, this.resolveListener);
            }
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        String text = "service lost "+serviceInfo.getServiceName();
        logger.info(text);
        String eventName = serviceInfo.getServiceName();
        if (eventName.toLowerCase().contains(serviceName.toLowerCase())) {
            DirectEndpoint device = serviceToUrl.remove(eventName);
            if (deviceEndpointListener != null)
                deviceEndpointListener.removed(device);
        }
    }

    NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            String text = "resolve failed for service: "+serviceInfo.getServiceName()+", error code: "+errorCode;
            logger.error(text);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            String text = "service: "+serviceInfo.getServiceName()+" resovled";
            logger.info(text);

            DirectEndpoint device = extractDevice(serviceInfo);
            serviceToUrl.put(serviceInfo.getServiceName(), device);
            if (deviceEndpointListener != null)
                deviceEndpointListener.added(device);
        }
    };

    private DirectEndpoint extractDevice(NsdServiceInfo serviceInfo) {
        String address = "unknown";
        InetAddress host = serviceInfo.getHost();
        if (host != null) {
            address = host.getHostAddress();
        }
        int port = serviceInfo.getPort();

        return new DirectEndpoint(address, Ssh.SSH_SERVER_PORT, "root", "syncloud", null);
    }
}
