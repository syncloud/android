package org.syncloud.platform.ssh;

import org.syncloud.platform.ssh.model.ConnectionPoint;

public interface ConnectionPointProvider {
    ConnectionPoint get();
}
