package org.syncloud.ssh;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DeviceEndpoint;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.SshResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.syncloud.ssh.EndpointChecker.findReachableEndpoint;

public class Ssh {

    public static final int SSH_SERVER_PORT = 22;

    //TODO: replace with execute1
    public static Result<SshResult> execute(Device device, List<String> commands) {
        try {
            Result<String> run = run(device, commands);
            if (run.hasError())
                return Result.error(run.getError());
            else
                return Result.value(new SshResult(0, run.getValue()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public static Result<String> execute1(Device device, List<String> commands) {
        try {
            return run(device, commands);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private static Result<String> run(Device device, List<String> commands) throws JSchException, IOException {

        JSch jsch = new JSch();

        Optional<DeviceEndpoint> reachableEndpoint = findReachableEndpoint(device);
        if (!reachableEndpoint.isPresent())
            return Result.error( "Unable to connect to the device");
        DeviceEndpoint endpoint = reachableEndpoint.get();
        Session session = jsch.getSession(endpoint.getLogin(), endpoint.getHost(), endpoint.getPort());
        if (endpoint.getKey() == null) {
            session.setPassword(endpoint.getPassword());
        } else {
            jsch.addIdentity(endpoint.getLogin(), endpoint.getKey().getBytes(), null, new byte[0]);
            session.setUserInfo(new UserInfo() {
                @Override
                public String getPassphrase() {
                    return null;
                }

                @Override
                public String getPassword() {
                    return null;
                }

                @Override
                public boolean promptPassword(String message) {
                    return false;
                }

                @Override
                public boolean promptPassphrase(String message) {
                    return false;
                }

                @Override
                public boolean promptYesNo(String message) {
                    return false;
                }

                @Override
                public void showMessage(String message) {

                }
            });
        }

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();

            ChannelExec channel = (ChannelExec)
                    session.openChannel("exec");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setOutputStream(baos);

            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channel.setErrStream(err);

            channel.setCommand(StringUtils.join(commands, "; "));
            InputStream inputStream = channel.getInputStream();

            try {
                channel.connect();
                String otput = new String(ByteStreams.toByteArray(inputStream));
                while (channel.getExitStatus() == -1) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
                //TODO: export output stream for progress monitor
                String message = baos.toString() + otput + err.toString();
                if (channel.getExitStatus() == 0)
                    return Result.value(message);
                else
                    return Result.error(message);
            } finally {
                if (channel.isConnected())
                    channel.disconnect();
            }
        } finally {
            if (session.isConnected())
                session.disconnect();
        }
    }

}
