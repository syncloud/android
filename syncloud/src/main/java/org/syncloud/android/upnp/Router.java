package org.syncloud.android.upnp;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

public class Router {

    private Logger logger = Logger.getLogger(Router.class);

    private GatewayDevice device;

    public Router(GatewayDevice device) {
        this.device = device;
    }

    public String getName() {
        return device.getModelName();
    }

    public Optional<String> getExternalIP() {
        try {
            return Optional.of(device.getExternalIPAddress());
        } catch (Exception e) {
            logger.error("unable to get external address, " + e.getMessage());
        }
        return Optional.absent();
    }

    public int getPortMappingsCount() {
        return getPortMappings().size();
    }

    private List<PortMappingEntry> getPortMappings() {
        List<PortMappingEntry> mappings = new ArrayList<PortMappingEntry>();
        int i = 0;
        while (true) {

            PortMappingEntry entry = new PortMappingEntry();
            boolean exists = false;
            try {
                exists = device.getGenericPortMappingEntry(i, entry);
            } catch (Exception e) {
                logger.error("unable to get mapping: " + e.getMessage());
            }

            if (exists) {
                mappings.add(entry);
            } else {
                break;
            }

            i++;
        }
        return mappings;
    }

    private Optional<Integer> getAvailableExternalPort() {
        ImmutableList<Integer> ports = from(getPortMappings())
                .transform(new Function<PortMappingEntry, Integer>() {
                    @Override
                    public Integer apply(PortMappingEntry input) {
                        return input.getExternalPort();
                    }
                }).toList();

        for (int i = 10000; i < 65536; i++) {
            if (!ports.contains(i)) {
                return Optional.of(i);
            }
        }

        return Optional.absent();
    }

    public boolean canManipulatePorts(String myIp) {
        final Optional<Integer> availableExternalPort = getAvailableExternalPort();
        if(!availableExternalPort.isPresent())
            return false;

        Integer port = availableExternalPort.get();
        logger.info("first available port: " + port);

        PortMappingEntry portMapping = new PortMappingEntry();
        portMapping.setExternalPort(port);
        portMapping.setInternalClient(myIp);
        portMapping.setProtocol("TCP");

        try {
            if(!device.addPortMapping(port, port, myIp, "TCP", "syncloud"))
                return false;
        } catch (Exception e) {
            logger.error("unable to add mapping: " + port);
            return false;
        }

        Optional<PortMappingEntry> foundAdded = findPortMapping(port);
        if(!foundAdded.isPresent())
            return false;

        logger.debug("found added: " + foundAdded.get().getExternalPort());

        try {
            if(!device.deletePortMapping(port, "TCP"))
                return false;
        } catch (Exception e) {
            logger.error("unable to add mapping: " + port);
            return false;
        }

        return !findPortMapping(port).isPresent();
    }

    private Optional<PortMappingEntry> findPortMapping(final int externalPort) {
        List<PortMappingEntry> portMappings = getPortMappings();
        return from(portMappings).firstMatch(new Predicate<PortMappingEntry>() {
            @Override
            public boolean apply(PortMappingEntry input) {
                return input.getExternalPort() == externalPort;
            }
        });
    }
}
