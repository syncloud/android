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
            logger.info("session connecting");
            session.connect();
            logger.info("session connected");

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
                logger.info("channel connecting");
                channel.connect();
                logger.info("reading output");
                String output = new String(ByteStreams.toByteArray(inputStream));
                logger.info("waiting for exit code");
                while (channel.getExitStatus() == -1) {
                    try {
                        logger.info("sleeping");
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        logger.error("got exception while sleeping", e);
                    }
                }
                int exitCode = channel.getExitStatus();
                logger.info("got exit code: " + exitCode);
                if (exitCode == 0)
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
