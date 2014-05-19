package org.syncloud.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.model.Device;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Ssh {

    public static final String USERNAME = "root";
    public static final String PASSWORD = "syncloud";
    public static final int SSH_SERVER_PORT = 22;


    public static Result<SshResult> execute(String hostname, List<String> commands) {
        try {
            return Result.value(run(new Device(hostname, SSH_SERVER_PORT), commands));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public static Result<SshResult> execute(Device device, List<String> commands) {
        try {
            return Result.value(run(device, commands));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private static SshResult run(Device device, List<String> commands) throws JSchException, IOException {

        JSch jsch = new JSch();

        Session session = jsch.getSession(device.getLogin(), device.getIp(), device.getPort());
        if (device.getKey() == null) {
            session.setPassword(device.getPassword());
        } else {
            jsch.addIdentity(device.getLogin(), device.getKey().getBytes(), null, new byte[0]);
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
