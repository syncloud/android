package org.syncloud.integration.discovery;

import org.syncloud.model.Device;

public interface DeviceListener {
    public void added(Device device);
    public void removed(Device device);
}
