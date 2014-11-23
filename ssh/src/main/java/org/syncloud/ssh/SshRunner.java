package org.syncloud.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.exec.LogOutputStream;
import org.apache.log4j.Logger;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SshRunner {

    private static Logger logger = Logger.getLogger(SshRunner.class);

    public Result<String> run(Endpoint endpoint, Credentials credentials, String command) throws JSchException, IOException {

        JSch jsch = new JSch();

        Session session = jsch.getSession(credentials.login(), endpoint.host(), endpoint.port());
        session.setTimeout(10000);
        logger.info("Endpoint: " + endpoint);
        if (credentials.key() == null) {
            logger.info("Password authentication");
            session.setPassword(credentials.password());
        } else {
            logger.info("Public key authentication");
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
                    logger.info(line);
                }
            });

            channel.setErrStream(new LogOutputStream() {
                @Override
                protected void processLine(String line, int level) {
                    logger.error(line);
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
                    logger.info(output);
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