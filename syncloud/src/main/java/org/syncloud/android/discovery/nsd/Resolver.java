package org.syncloud.android.discovery.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Endpoint;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

public class Resolver {
    private static Logger logger = LogManager.getLogger(Resolver.class.getName());

    private DeviceEndpointListener deviceEndpointListener;
    private NsdManager manager;
    private boolean isBusy = false;
    private Queue<NsdServiceInfo> queue = new LinkedList<NsdServiceInfo>();
    private ResolveListener resolveListener;

    public class QueueItem {
        public String serviceName;
        public NsdServiceInfo serviceInfo;

        public QueueItem(String serviceName, NsdServiceInfo serviceInfo) {
            this.serviceName = serviceName;
            this.serviceInfo = serviceInfo;
        }
    }

    public Resolver(NsdManager manager, DeviceEndpointListener deviceEndpointListener) {
        this.manager = manager;
        this.deviceEndpointListener = deviceEndpointListener;
        this.resolveListener = new ResolveListener();
    }

    public void resolve(NsdServiceInfo serviceInfo) {
        queue.add(serviceInfo);
        checkQueue();
    }

    private synchronized void checkQueue() {
        if (isBusy) return;
        NsdServiceInfo serviceInfo = queue.poll();
        if (serviceInfo != null) {
            isBusy = true;
            manager.resolveService(serviceInfo, resolveListener);

        }
    }

    private void endResolving() {
        isBusy = false;
        checkQueue();
    }

    private void deviceFound(Endpoint device) {
        if (deviceEndpointListener != null)
            deviceEndpointListener.added(device);
    }

    public class ResolveListener implements NsdManager.ResolveListener {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            String text = "resolve failed for service: "+serviceInfo.getServiceName()+", error code: "+errorCode;
            logger.error(text);
            endResolving();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            String serviceName = serviceInfo.getServiceName();
            String text = "service: "+serviceName+" resovled";
            logger.info(text);

            InetAddress host = serviceInfo.getHost();
            if (host != null) {
                String address = host.getHostAddress();
                if (!address.contains(":")) {
                    int port = serviceInfo.getPort();
                    Endpoint device = new Endpoint(address, Ssh.SSH_SERVER_PORT);
                    deviceFound(device);
                }
            }
            endResolving();
        }

    }
}
