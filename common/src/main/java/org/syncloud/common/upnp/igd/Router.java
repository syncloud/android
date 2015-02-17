package org.syncloud.common.upnp.igd;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.model.PortMapping;
import org.syncloud.common.upnp.igd.action.GetExternalIPImpl;
import org.syncloud.common.upnp.igd.action.GetPortMappingEntryImpl;
import org.syncloud.common.upnp.igd.action.PortMappingAddImpl;
import org.syncloud.common.upnp.igd.action.PortMappingDeleteImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.collect.FluentIterable.from;

public class Router {

    public int timeout;
    private Logger logger = Logger.getLogger(Router.class);

    private Registry registry;
    private Service service;
    private Device device;
    public static final int LIMIT = 10000;

    public Router(Registry registry, Device device, Service service, int timeout) {
        this.registry = registry;
        this.device = device;
        this.service = service;
        this.timeout = timeout;
    }

    public String getName() {
        return device.getDisplayString();
    }

    public Optional<String> getExternalIP() {
        return sync(registry.getUpnpService(), new GetExternalIPImpl(service)).getIp();
    }

    public List<PortMapping> getPortMappings() {

        ArrayList<PortMapping> mappings = new ArrayList<>();
        long i = 0;
        while (i < LIMIT) {

            Optional<PortMapping> mapping = sync(
                    registry.getUpnpService(),
                    new GetPortMappingEntryImpl(service, i)
            ).getPortMapping();

            if (!mapping.isPresent())
                break;

            mappings.add(mapping.get());
            i++;
        }
        logger.info("Found: " + mappings.size() + " port mappings");
        return mappings;

    }

    private Optional<Long> getAvailableExternalPort() {
        ImmutableList<Long> ports = from(getPortMappings())
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

    public boolean addPortMapping(PortMapping portMapping) {
        return sync(
                registry.getUpnpService(),
                new PortMappingAddImpl(portMapping, service)
        ).isSuccessfull();
    }

    public boolean removePortMapping(PortMapping portMapping) {
        return sync(
                registry.getUpnpService(),
                new PortMappingDeleteImpl(portMapping, service)
        ).isSuccessfull();
    }

    private <T extends ActionCallback> T sync(UpnpService upnpService, T callback) {
        try {
            upnpService.getControlPoint().execute(callback).get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("interrupted: " + e.getMessage());
        }
        return callback;
    }

    public boolean canToManipulatePorts(String myIp) {

        final Optional<Long> availableExternalPort = getAvailableExternalPort();
        if(!availableExternalPort.isPresent())
            return false;

        logger.info("first available port: " + availableExternalPort.get());

        PortMapping portMapping = new PortMapping(
                availableExternalPort.get().intValue(),
                myIp,
                PortMapping.Protocol.TCP);

        if(!addPortMapping(portMapping))
            return false;

        Optional<PortMapping> foundAdded = findPortMapping(availableExternalPort);
        if(!foundAdded.isPresent())
            return false;

        logger.debug("foundAdded: " + foundAdded.get());

        if(!removePortMapping(portMapping))
            return false;

        return !findPortMapping(availableExternalPort).isPresent();

    }

    private Optional<PortMapping> findPortMapping(final Optional<Long> availableExternalPort) {
        List<PortMapping> portMappings = getPortMappings();
        return from(portMappings).firstMatch(new Predicate<PortMapping>() {
            @Override
            public boolean apply(PortMapping input) {
                return input.getExternalPort().getValue().equals(availableExternalPort.get());
            }
        });
    }
}