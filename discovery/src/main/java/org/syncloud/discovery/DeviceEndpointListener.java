package org.syncloud.discovery;

import org.syncloud.ssh.model.DeviceEndpoint;

public interface DeviceEndpointListener {
    public void added(DeviceEndpoint endpoint);
    public void removed(DeviceEndpoint endpoint);
}
