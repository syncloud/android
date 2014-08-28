package org.syncloud.ssh;

import com.google.common.io.ByteStreams;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.DirectEndpoint;

import java.io.IOException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;

public class Scp {
    public static Result<String> getFile(DirectEndpoint endpoint, String file) {

        Connection conn = new Connection(endpoint.getHost(), endpoint.getPort());

        try {
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
            try {
                conn.close();
            } catch (Exception ignore) {
            }
        }
    }


}
