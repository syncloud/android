package org.syncloud.common.check;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.igd.callback.GetExternalIP;
import org.syncloud.common.model.UPnPStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class UPnP {

    private static Logger logger = Logger.getLogger(UPnP.class);
    CountDownLatch startSignal = new CountDownLatch(1);

    public static final DeviceType IGD_DEVICE_TYPE = new UDADeviceType("InternetGatewayDevice", 1);
    public static final DeviceType CONNECTION_DEVICE_TYPE = new UDADeviceType("WANConnectionDevice", 1);

    public static final ServiceType IP_SERVICE_TYPE = new UDAServiceType("WANIPConnection", 1);
    public static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);

    public Optional<UPnPStatus> checkStatus(long seconds) {
        return checkStatus(seconds, new DefaultUpnpServiceConfiguration());
    }

    public Optional<UPnPStatus> checkStatus(long seconds, UpnpServiceConfiguration configuration) {

        StatusListener statusListener = new StatusListener();
        UpnpService upnpService = new UpnpServiceImpl(configuration, statusListener);
        upnpService.getControlPoint().search();

        try {
            startSignal.await(seconds, TimeUnit.SECONDS);
            return Optional.fromNullable(statusListener.status);
        } catch (InterruptedException e) {
            logger.error("interrupted", e);
            return Optional.absent();
        }
    }

    //TODO: copied from org.fourthline.cling.support.igd.PortMappingListener
    protected Service discoverConnectionService(Device device) {
        if (!device.getType().equals(IGD_DEVICE_TYPE)) {
            return null;
        }

        Device[] connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE);
        if (connectionDevices.length == 0) {
            logger.debug("IGD doesn't support '" + CONNECTION_DEVICE_TYPE + "': " + device);
            return null;
        }

        Device connectionDevice = connectionDevices[0];
        logger.debug("Using first discovered WAN connection device: " + connectionDevice);

        Service ipConnectionService = connectionDevice.findService(IP_SERVICE_TYPE);
        Service pppConnectionService = connectionDevice.findService(PPP_SERVICE_TYPE);

        if (ipConnectionService == null && pppConnectionService == null) {
            logger.debug("IGD doesn't support IP or PPP WAN connection service: " + device);
        }

        return ipConnectionService != null ? ipConnectionService : pppConnectionService;
    }

    private class StatusListener extends DefaultRegistryListener {
        public UPnPStatus status;

        @Override
        public synchronized void deviceAdded(Registry registry, final Device device) {

            logger.info("deviceAdded: " + device.getDisplayString());

            Service connectionService;
            if ((connectionService = discoverConnectionService(device)) == null)
                return;

            logger.info("service: " + connectionService);


            UpnpService upnpService = registry.getUpnpService();
            upnpService.getControlPoint().execute(
                    new GetExternalIP(connectionService) {

                        @Override
                        protected void success(String externalIPAddress) {
                            status = new UPnPStatus(device.getDisplayString(), externalIPAddress);
                            startSignal.countDown();
                        }

                        @Override
                        public void failure(ActionInvocation invocation,
                                            UpnpResponse operation,
                                            String defaultMsg) {
                            logger.info(defaultMsg);
                        }
                    }
            );


        }
    }

}
