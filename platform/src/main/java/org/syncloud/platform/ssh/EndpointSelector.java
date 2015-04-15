package org.syncloud.platform.ssh;

import org.syncloud.platform.ssh.model.ConnectionPoint;
import org.syncloud.platform.ssh.model.Device;

public class EndpointSelector {

    private EndpointPreference preference;

    public EndpointSelector(EndpointPreference preference) {
        this.preference = preference;
    }

    public ConnectionPoint select(Device device, boolean first) {
        boolean valid = first ? preference.isRemote() : !preference.isRemote();
        if (valid){
            return new ConnectionPoint(device.remoteEndpoint(), device.credentials());
        } else
            return new ConnectionPoint(device.localEndpoint(), device.credentials());

    }
}
