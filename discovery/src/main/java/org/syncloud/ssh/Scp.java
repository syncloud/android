package org.syncloud.ssh;

import com.google.common.io.ByteStreams;

import org.syncloud.model.Device;
import org.syncloud.model.Result;

import java.io.IOException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;

public class Scp {
    public static Result<String> getFile(Device device, String file) {
        try {
            final Connection conn = new Connection(device.getHost(), device.getPort());
            conn.connect(null, 5000, 0);
            if (device.getKey() != null)
                conn.authenticateWithPublicKey(device.getLogin(), device.getKey().toCharArray(), null);
            else
                conn.authenticateWithPassword(device.getLogin(), device.getPassword());
            final SCPClient scp_client = new SCPClient(conn);
            SCPInputStream scpInputStream = scp_client.get(file);

            return Result.value(new String(ByteStreams.toByteArray(scpInputStream)));


        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }
}
