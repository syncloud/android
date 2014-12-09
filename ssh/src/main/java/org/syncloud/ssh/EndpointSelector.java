package org.syncloud.ssh;

import com.google.common.base.Optional;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

public class EndpointSelector {

    public static final String SSH_TYPE = "_ssh._tcp";

    private EndpointResolver resolver;
    private EndpointPreference preference;

    public EndpointSelector(EndpointResolver resolver, EndpointPreference preference) {
        this.resolver = resolver;
        this.preference = preference;
    }

    public Result<Endpoint> first(Device device) {
        return select(device, true);
    }

    public Result<Endpoint> second(Device device) {
        return select(device, false);
    }

    private Result<Endpoint> select(Device device, boolean first) {

        boolean valid = first ? preference.isRemote() : !preference.isRemote();
        if (valid){
            String domain = device.userDomain();
            Optional<Endpoint> endpointOptional = resolver.dnsService(domain, SSH_TYPE);
            if (endpointOptional.isPresent())
                return Result.value(endpointOptional.get());
            else
                return Result.error("Public address is not available yet for " + domain + ", " + SSH_TYPE);
        } else
            return Result.value(device.localEndpoint());

    }
}
