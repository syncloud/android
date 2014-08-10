package org.syncloud.ssh;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;

import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DeviceEndpoint;
import org.syncloud.common.model.Result;

import java.io.IOException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;

import static com.google.common.collect.Iterables.find;
import static org.syncloud.ssh.EndpointChecker.findReachableEndpoint;

public class Scp {
    public static Result<String> getFile(Device device, String file) {

        Connection conn = null;

        try {

            Optional<DeviceEndpoint> reachableEndpoint = findReachableEndpoint(device);
            if (!reachableEndpoint.isPresent())
                return Result.error("Unable to connect to the device");
            DeviceEndpoint endpoint = reachableEndpoint.get();

            conn = new Connection(endpoint.getHost(), endpoint.getPort());
            conn.connect(null, 5000, 0);
            if (endpoint.getKey() != null)
                conn.authenticateWithPublicKey(endpoint.getLogin(), endpoint.getKey().toCharArray(), null);
            else
                conn.authenticateWithPassword(endpoint.getLogin(), endpoint.getPassword());
            final SCPClient scp_client = new SCPClient(conn);
            SCPInputStream scpInputStream = scp_client.get(file);

            String key = new String(ByteStreams.toByteArray(scpInputStream));
            scpInputStream.close();

            return Result.value(key);


        } catch (IOException e) {
            return Result.error(e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }
            }
        }
    }


}
