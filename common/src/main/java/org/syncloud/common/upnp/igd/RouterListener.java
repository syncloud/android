package org.syncloud.common.upnp.igd;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RouterListener extends DefaultRegistryListener {

    private static Logger logger = Logger.getLogger(RouterListener.class);
    private CountDownLatch countDownLatch;
    private Optional<Router> router = Optional.absent();

    public RouterListener() {
        reset();
    }

    @Override
    public synchronized void deviceAdded(Registry registry, final Device device) {

        logger.info("deviceAdded: " + device.getDisplayString());
        Optional<Service> service = discoverConnectionService(device);
        if (service.isPresent()) {
            logger.info("detected router service: " + service.get().getServiceId().getId());
            router = Optional.of(new Router(registry, device, service.get()));
            countDownLatch.countDown();
        }
    }

    public void reset() {
        countDownLatch = new CountDownLatch(1);
    }

    public Optional<Router> await(long seconds) {
        try {
            countDownLatch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("interrupted: " + e.getMessage(), e);
        }

        return router;
    }

    public static final DeviceType IGD_DEVICE_TYPE = new UDADeviceType("InternetGatewayDevice", 1);
    public static final DeviceType CONNECTION_DEVICE_TYPE = new UDADeviceType("WANConnectionDevice", 1);

    public static final ServiceType IP_SERVICE_TYPE = new UDAServiceType("WANIPConnection", 1);
    public static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);

    //TODO: copied from org.fourthline.cling.support.igd.PortMappingListener
    private Optional<Service> discoverConnectionService(Device device) {
        if (!device.getType().equals(IGD_DEVICE_TYPE)) {
            return Optional.absent();
        }

        Device[] connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE);
        if (connectionDevices.length == 0) {
            logger.debug("IGD doesn't support '" + CONNECTION_DEVICE_TYPE + "': " + device);
            return Optional.absent();
        }

        Device connectionDevice = connectionDevices[0];
        logger.debug("Using first discovered WAN connection device: " + connectionDevice);

        Service ipConnectionService = connectionDevice.findService(IP_SERVICE_TYPE);
        Service pppConnectionService = connectionDevice.findService(PPP_SERVICE_TYPE);

        if (ipConnectionService == null && pppConnectionService == null) {
            logger.debug("IGD doesn't support IP or PPP WAN connection service: " + device);
        }

        return Optional.fromNullable(ipConnectionService != null ? ipConnectionService : pppConnectionService);
    }
}
