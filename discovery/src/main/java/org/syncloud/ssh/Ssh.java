package org.syncloud.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.model.SshResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

//TODO: Can be moved out of android module
public class Ssh {

    public static final String USERNAME = "root";
    public static final String PASSWORD = "syncloud";


    public static SshResult execute(String hostname, List<String> commands) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(USERNAME, hostname, 22);
        session.setPassword(PASSWORD);

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        ChannelExec channel = (ChannelExec)
                session.openChannel("exec");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channel.setOutputStream(baos);

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        channel.setErrStream(err);

        channel.setCommand(StringUtils.join(commands, "; "));
        InputStream inputStream = channel.getInputStream();

        channel.connect();
        String otput = new String(ByteStreams.toByteArray(inputStream));
        while (channel.getExitStatus() == -1) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
        }
        int exitStatus = channel.getExitStatus();
        channel.disconnect();
        session.disconnect();

        //TODO: export output stream for progress monitor

        return new SshResult(exitStatus, baos.toString() + otput + err.toString());
    }


}
