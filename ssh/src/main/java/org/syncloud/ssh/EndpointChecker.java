package org.syncloud.ssh;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DeviceEndpoint;

import java.io.IOException;
import java.net.Socket;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Iterables.find;

public class EndpointChecker {
    public static Optional<DeviceEndpoint> findReachableEndpoint(Device device) {
        return fromNullable(find(device.endpoints(), new Predicate<DeviceEndpoint>() {
            @Override
            public boolean apply(DeviceEndpoint endpoint) {
                try {
                    new Socket(endpoint.getHost(), endpoint.getPort()).close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }, null));
    }
}
