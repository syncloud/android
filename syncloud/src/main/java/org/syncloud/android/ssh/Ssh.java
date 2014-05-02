package org.syncloud.android.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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

    public static String install(String hostname, String app) throws Exception {

        String fullAppName = "/data/syncloud/apps/" + app;
        return execute(hostname, asList(
                "wget -qO- " + REPO_URL + "/system/download_repo.sh | sh",
                "chmod +x " + fullAppName,
                fullAppName + " install"
        ));
    }

    public static String remove(String hostname, String app) throws Exception {

        String fullAppName = "/data/syncloud/apps/" + app;

        return execute(hostname, asList(
               fullAppName + " remove"
        ));
    }

    private static String execute(String hostname, List<String> commands) throws JSchException, IOException {
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
        channel.disconnect();
        session.disconnect();

        return baos.toString() + otput + err.toString();
    }


}
