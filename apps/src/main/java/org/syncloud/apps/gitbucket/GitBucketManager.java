package org.syncloud.apps.gitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.common.StringResult;
import org.syncloud.common.SyncloudException;

import java.io.IOException;

import static org.syncloud.platform.ssh.SshRunner.cmd;

public class GitBucketManager {
    private static Logger logger = Logger.getLogger(GitBucketManager.class);

    private static String CTL_BIN = "gitbucket-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;

    public GitBucketManager() {
        this.ssh = new SshRunner();
    }

    public String enable(ConnectionPointProvider connectionPoint, String login, String password) {
        return ssh.run(connectionPoint, cmd(CTL_BIN, "enable", login, password));
    }

    public String disable(ConnectionPointProvider connectionPoint) {
        return ssh.run(connectionPoint, cmd(CTL_BIN, "disable"));
    }

    public String url(ConnectionPointProvider connectionPoint) {
        String json = ssh.run(connectionPoint, cmd(CTL_BIN, "url"));
        try {
            return JSON.readValue(json, StringResult.class).data;
        } catch (IOException e) {
            String message = "Unable to parse gitbucket url response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }
}
