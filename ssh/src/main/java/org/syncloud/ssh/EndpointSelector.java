package org.syncloud.ssh;

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
        if (valid)
            return resolver.dnsService(device.userDomain(), SSH_TYPE);
        else
            return Result.value(device.localEndpoint());

    }
}
