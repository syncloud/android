package org.syncloud.ssh;

import org.syncloud.ssh.model.ConnectionPoint;

public class SimpleConnectionPointProvider implements ConnectionPointProvider {

    public static ConnectionPointProvider simple(ConnectionPoint connectionPoint) {
        return new SimpleConnectionPointProvider(connectionPoint);
    }

    private ConnectionPoint connectionPoint;

    public SimpleConnectionPointProvider(ConnectionPoint connectionPoint) {
        this.connectionPoint = connectionPoint;
    }

    @Override
    public ConnectionPoint get() {
        return null;
    }
}
