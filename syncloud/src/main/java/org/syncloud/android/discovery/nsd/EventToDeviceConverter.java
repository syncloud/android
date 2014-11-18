package org.syncloud.android.discovery.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Endpoint;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class EventToDeviceConverter implements NsdManager.DiscoveryListener {

    private static Logger logger = LogManager.getLogger(EventToDeviceConverter.class.getName());

    private NsdManager manager;
    private String serviceName;
    private DeviceEndpointListener deviceEndpointListener;
    private Map<String, Endpoint> serviceToUrl = new HashMap<String, Endpoint>();

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
        String serviceName = serviceInfo.getServiceName();
        String text = "service found "+serviceName;
        logger.info(text);
        if (!serviceToUrl.containsKey(serviceName)) {
            if (serviceName.toLowerCase().contains(serviceName.toLowerCase())) {
                serviceToUrl.put(serviceName, null);
                text = "starting resolving service " + serviceName;
                logger.info(text);
                manager.resolveService(serviceInfo, this.resolveListener);
            }
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        String text = "service lost "+serviceInfo.getServiceName();
        logger.info(text);
        String serviceName = serviceInfo.getServiceName();
        if (serviceName.toLowerCase().contains(this.serviceName.toLowerCase())) {
            Endpoint device = serviceToUrl.remove(serviceName);
            if (deviceEndpointListener != null && device != null)
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
            String serviceName = serviceInfo.getServiceName();
            String text = "service: "+serviceName+" resovled";
            logger.info(text);

            Endpoint device = extractDevice(serviceInfo);
            serviceToUrl.put(serviceName, device);
            if (deviceEndpointListener != null)
                deviceEndpointListener.added(device);
        }
    };

    private Endpoint extractDevice(NsdServiceInfo serviceInfo) {
        String address = "unknown";
        InetAddress host = serviceInfo.getHost();
        if (host != null) {
            address = host.getHostAddress();
        }
        int port = serviceInfo.getPort();

        return new Endpoint(address, Ssh.SSH_SERVER_PORT);
    }
}
