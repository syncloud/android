package org.syncloud.common.upnp.igd;

import com.google.common.base.Optional;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.model.PortMapping;
import org.syncloud.common.upnp.igd.action.GetExternalIPSync;
import org.syncloud.common.upnp.igd.action.GetPortMappingEntrySync;

import java.util.ArrayList;
import java.util.List;

public class Router {

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

}