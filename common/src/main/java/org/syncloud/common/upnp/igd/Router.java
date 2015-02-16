package org.syncloud.common.upnp.igd;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.igd.callback.PortMappingAdd;
import org.fourthline.cling.support.igd.callback.PortMappingDelete;
import org.fourthline.cling.support.model.PortMapping;
import org.syncloud.common.upnp.igd.action.GetExternalIPSync;
import org.syncloud.common.upnp.igd.action.GetPortMappingEntrySync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.collect.FluentIterable.from;

public class Router {

    private Logger logger = Logger.getLogger(Router.class);

    private Registry registry;
    private Service service;
    private Device device;
    public static final int LIMIT = 10000;

    public Router(Registry registry, Device device, Service service) {
        this.registry = registry;
        this.device = device;
        this.service = service;
    }

    public String getName() {
        return device.getDisplayString();
    }

    public Optional<String> getExternalIP(long seconds) {

        UpnpService upnpService = registry.getUpnpService();
        GetExternalIPSync callback = new GetExternalIPSync(service);
        upnpService.getControlPoint().execute(callback);
        return callback.await(seconds);

    }

    public List<PortMapping> getPortMappings(long seconds) {

        ArrayList<PortMapping> mappings = new ArrayList<>();
        UpnpService upnpService = registry.getUpnpService();
        long i = 0;
        while (i < LIMIT) {
            GetPortMappingEntrySync callback = new GetPortMappingEntrySync(service, i);
            upnpService.getControlPoint().execute(callback);
            Optional<PortMapping> mapping = callback.await(seconds);
            if (!mapping.isPresent())
                break;
            mappings.add(mapping.get());
            i++;
        }

        return mappings;

    }

    public Optional<Long> getAvailableExternalPort(long seconds) {
        ImmutableList<Long> ports = from(getPortMappings(seconds))
                .transform(new Function<PortMapping, Long>() {
                    @Override
                    public Long apply(PortMapping input) {
                        return input.getExternalPort().getValue();
                    }
                }).toList();

        for (long i = 10000; i < 65536; i++) {
            if (!ports.contains(i)) {
                return Optional.of(i);
            }
        }

        return Optional.absent();
    }

    public boolean addPortMapping(long seconds, PortMapping portMapping) {

        UpnpService upnpService = registry.getUpnpService();
        logger.debug("adding: " + portMapping);
        MyPortMappingAdd callback = new MyPortMappingAdd(portMapping, service);
        try {
            upnpService.getControlPoint().execute(callback).get(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("interrupted: " + e.getMessage());
        }

        return callback.isSuccessfull();
    }

    private class MyPortMappingAdd extends PortMappingAdd {
        private Logger logger = Logger.getLogger(MyPortMappingAdd.class);

        private boolean successfull = false;

        public MyPortMappingAdd(PortMapping portMapping, Service service) {
            super(service, portMapping);
        }

        @Override
        public void success(ActionInvocation invocation) {
            successfull = true;
        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            logger.error("failed: " + operation.getResponseDetails());
            logger.error("reason: " + defaultMsg);
            successfull = false;
        }

        public boolean isSuccessfull() {
            return successfull;
        }
    }

    public boolean removePortMapping(long seconds, PortMapping portMapping) {

        UpnpService upnpService = registry.getUpnpService();
        logger.debug("removing: " + portMapping);
        MyPortMappingDelete callback = new MyPortMappingDelete(portMapping, service);
        try {
            upnpService.getControlPoint().execute(callback).get(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("interrupted: " + e.getMessage());
        }

        return callback.isSuccessfull();
    }

    private class MyPortMappingDelete extends PortMappingDelete {
        private Logger logger = Logger.getLogger(MyPortMappingAdd.class);

        private boolean successfull = false;

        public MyPortMappingDelete(PortMapping portMapping, Service service) {
            super(service, portMapping);
        }

        @Override
        public void success(ActionInvocation invocation) {
            successfull = true;
        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            logger.error("failed: " + operation.getResponseDetails());
            logger.error("reason: " + defaultMsg);
            successfull = false;
        }

        public boolean isSuccessfull() {
            return successfull;
        }
    }

    public boolean canToManipulatePorts(int seconds, String myIp) {

        final Optional<Long> availableExternalPort = getAvailableExternalPort(seconds);
        if(!availableExternalPort.isPresent())
            return false;

        logger.info("first available port: " + availableExternalPort.get());

        PortMapping portMapping = new PortMapping(
                availableExternalPort.get().intValue(),
                myIp,
                PortMapping.Protocol.TCP);

        if(!addPortMapping(10,portMapping))
            return false;

        Optional<PortMapping> foundAdded = findPortMapping(availableExternalPort);
        if(!foundAdded.isPresent())
            return false;

        logger.debug("foundAdded: " + foundAdded.get());

        if(!removePortMapping(10, portMapping))
            return false;

        return !findPortMapping(availableExternalPort).isPresent();

    }

    private Optional<PortMapping> findPortMapping(final Optional<Long> availableExternalPort) {
        List<PortMapping> portMappings = getPortMappings(10);
        return from(portMappings).firstMatch(new Predicate<PortMapping>() {
            @Override
            public boolean apply(PortMapping input) {
                return input.getExternalPort().getValue().equals(availableExternalPort.get());
            }
        });
    }
}