package org.syncloud.ssh;

import org.syncloud.ssh.model.ConnectionPoint;

public interface ConnectionPointProvider {
    ConnectionPoint get();
}
