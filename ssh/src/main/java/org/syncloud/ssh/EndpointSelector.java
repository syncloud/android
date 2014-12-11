package org.syncloud.ssh;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

public class EndpointSelector {

    private static Logger logger = Logger.getLogger(EndpointSelector.class);

    public static final String SSH_TYPE = "_ssh._tcp";

    private EndpointResolver resolver;
    private EndpointPreference preference;

    public EndpointSelector(EndpointResolver resolver, EndpointPreference preference) {
        this.resolver = resolver;
        this.preference = preference;
    }

    public Optional<Endpoint> select(Device device, boolean first) {

        boolean valid = first ? preference.isRemote() : !preference.isRemote();
        if (valid){
            String domain = device.userDomain();
            Optional<Endpoint> endpoint = resolver.dnsService(domain, SSH_TYPE);
            if (!endpoint.isPresent())
                logger.error("Public address is not available yet for " + domain + ", " + SSH_TYPE);
            return endpoint;
        } else
            return Optional.of(device.localEndpoint());

    }
}
