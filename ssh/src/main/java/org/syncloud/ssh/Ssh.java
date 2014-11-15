package org.syncloud.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.syncloud.common.progress.NullProgress;
import org.syncloud.common.progress.Progress;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
import org.syncloud.common.model.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;

public class Ssh {

    public static final int SSH_SERVER_PORT = 22;
    public static final String SSH_TYPE = "_ssh._tcp";
    private JSch jsch;
    private final EndpointResolver resolver;

    public Ssh(JSch jsch, EndpointResolver resolver1) {
        this.jsch = jsch;
        resolver = resolver1;
    }

    public Result<String> execute(Device device, String command) {
        return execute(device, asList(command), new NullProgress());
    }

    public Result<String> execute(Device device, String command, Progress progress) {
        progress.progress("ssh: " + command);
        return execute(device, asList(command), progress);
    }

    private Result<String> execute(Device device, List<String> commands, Progress progress) {

        DirectEndpoint local = device.getLocalEndpoint();
        try {
            return run(local, commands, progress);
        } catch (Exception e) {
            progress.progress("Local endpoint is not available: " + local + ", error: " + e.getMessage());
        }

        Result<DirectEndpoint> resolved = resolver.dnsService(device.getUserDomain(), SSH_TYPE, local.getKey());
        if (resolved.hasError()) {
            progress.error("Unable to resolve dns: " + resolved.getError());
            return Result.error(resolved.getError());
        }

        DirectEndpoint remote = resolved.getValue();
        try {
            return run(remote, commands, progress);
        } catch (Exception e) {
            progress.error("Remote endpoint is not available: " + remote + ", error: " + e.getMessage());
        }

        return Result.error("unable to connect");
    }

    private Result<String> run(DirectEndpoint endpoint, List<String> commands, final Progress progress) throws JSchException, IOException {

        Session session = jsch.getSession(endpoint.getLogin(), endpoint.getHost(), endpoint.getPort());
        session.setTimeout(3000);
        if (endpoint.getKey() == null) {
            session.setPassword(endpoint.getPassword());
        } else {
            jsch.addIdentity(endpoint.getLogin(), endpoint.getKey().getBytes(), null, new byte[0]);
            session.setUserInfo(new EmptyUserInfo());
        }

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            channel.setOutputStream(new LogOutputStream() {
                @Override
                protected void processLine(String line, int level) {
                    progress.progress(line);
                }
            });

            channel.setErrStream(new LogOutputStream() {
                @Override
                protected void processLine(String line, int level) {
                    progress.error(line);
                }
            });

            channel.setCommand(StringUtils.join(commands, "; "));
            InputStream inputStream = channel.getInputStream();

            try {
                channel.connect();
                String output = new String(ByteStreams.toByteArray(inputStream));
                while (channel.getExitStatus() == -1) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
                //TODO: export output stream for progress monitor
                if (channel.getExitStatus() == 0)
                    return Result.value(output);
                else {
                    progress.error(output);
                    return Result.error(output);
                }
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
