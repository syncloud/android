package org.syncloud.android.discovery;

import org.syncloud.android.core.platform.model.Endpoint;

public interface DeviceEndpointListener {
    public void added(Endpoint endpoint);
    public void removed(Endpoint endpoint);
}
