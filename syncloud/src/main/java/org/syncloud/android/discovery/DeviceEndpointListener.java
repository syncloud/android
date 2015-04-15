package org.syncloud.android.discovery;

import org.syncloud.platform.ssh.model.Endpoint;

public interface DeviceEndpointListener {
    public void added(Endpoint endpoint);
    public void removed(Endpoint endpoint);
}
