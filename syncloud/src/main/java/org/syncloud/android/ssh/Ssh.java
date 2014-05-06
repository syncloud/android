package org.syncloud.android.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.syncloud.android.model.InstallStatus;
import org.syncloud.android.model.SshResult;
import org.syncloud.android.model.VerifyStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

public class Ssh {

    public static final String USERNAME = "root";
    public static final String PASSWORD = "syncloud";
    public static final String REPO_URL = "https://raw.githubusercontent.com/syncloud/apps/master";
    public static final String UPDATE_REPO = "wget -qO- " + REPO_URL + "/system/download_repo.sh | sh";
    public static final String REPO_DIR = "/data/syncloud/repo";
    public static final String APPS_DIR = REPO_DIR + "/apps";
    public static final String INSTALLER = APPS_DIR + "/system/syncloud.sh";

    public static String install(String hostname, String app) throws Exception {

        SshResult result = execute(hostname, asList(
                UPDATE_REPO,
                INSTALLER + " install " + app
        ));
        return result.getMessage();
    }

    public static InstallStatus status(String hostname, String app) throws Exception {
        SshResult result = execute(hostname, asList(INSTALLER + " status " + app));
        return new InstallStatus(result.getExitCode() == 0, "", result.getMessage());
    }

    public static VerifyStatus verify(String hostname, String app) throws Exception {
        SshResult result = execute(hostname, asList(INSTALLER + " verify " + app));
        return new VerifyStatus(result.getExitCode() == 0, result.getMessage());
    }

    public static String remove(String hostname, String app) throws Exception {

        SshResult result = execute(hostname, asList(INSTALLER + " remove " + app));
        return result.getMessage();
    }

    private static SshResult execute(String hostname, List<String> commands) throws JSchException, IOException {
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

        channel.setCommand(join(commands, "; "));
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
