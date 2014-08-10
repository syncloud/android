package org.syncloud.discovery;

import org.syncloud.ssh.model.Device;

public interface DeviceListener {
    public void added(Device device);
    public void removed(Device device);
}
