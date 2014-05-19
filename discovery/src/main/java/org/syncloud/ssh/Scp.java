package org.syncloud.ssh;

import com.google.common.io.ByteStreams;

import org.syncloud.model.Result;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;

public class Scp {
    public static Result<String> getFile(String hostname, String file) {
        try {
            final Connection conn = new Connection(hostname, 22);
            conn.connect(null, 5000, 0);
            conn.authenticateWithPassword(Ssh.USERNAME, Ssh.PASSWORD);
            final SCPClient scp_client = new SCPClient(conn);
            SCPInputStream scpInputStream = scp_client.get(file);

            return Result.value(new String(ByteStreams.toByteArray(scpInputStream)));


        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }
}
