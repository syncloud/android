package org.syncloud.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.exec.LogOutputStream;
import org.syncloud.common.progress.NullProgress;
import org.syncloud.common.progress.Progress;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.common.model.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Ssh {

    public static final int SSH_SERVER_PORT = 22;
    public static final String SSH_TYPE = "_ssh._tcp";
    private JSchFactory jSchFactory;
    private final EndpointResolver resolver;

    private Progress progress;
    
    public Ssh(JSchFactory jsch, EndpointResolver resolver, Progress progress) {
        this.jSchFactory = jsch;
        this.resolver = resolver;
        this.progress = progress;
    }

    public Result<String> execute(Device device, String command) {

        progress.progress("ssh: " + command);
        try {
            return run(device.localEndpoint(), device.credentials(), command);
        } catch (Exception e) {
            progress.progress("Local endpoint is not available: " + e.getMessage());
        }

        Result<Endpoint> remote = resolver.dnsService(device.userDomain(), SSH_TYPE);
        if (remote.hasError()) {
            progress.error("Unable to resolve dns: " + remote.getError());
            return Result.error(remote.getError());
        }

        Endpoint remote1 = remote.getValue();
        try {
            return run(remote1, device.credentials(), command);
        } catch (Exception e) {
            progress.error("Remote endpoint is not available: " + remote + ", error: " + e.getMessage());
        }

        return Result.error("unable to connect");
    }

    public Result<String> run(Endpoint endpoint, Credentials credentials, String command) throws JSchException, IOException {

        JSch jsch = jSchFactory.create();

        Session session = jsch.getSession(credentials.login(), endpoint.host(), endpoint.port());
        session.setTimeout(3000);
        if (credentials.key() == null) {
            session.setPassword(credentials.password());
        } else {
            jsch.addIdentity(credentials.login(), credentials.key().getBytes(), null, new byte[0]);
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

            channel.setCommand(command);
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
