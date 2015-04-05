package org.syncloud.apps.owncloud;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.StringResult;
import org.syncloud.common.SyncloudException;

import java.io.IOException;

import static org.syncloud.platform.ssh.SshRunner.cmd;

public class OwncloudManager {

    private static Logger logger = Logger.getLogger(OwncloudManager.class);

    private static String OWNCLOUD_CTL_BIN = "owncloud-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;

    public OwncloudManager() {
        this.ssh = new SshRunner();
    }

    public String finishSetup(ConnectionPointProvider connectionPoint, String login, String password, String protocol) {
        return ssh.run(connectionPoint, cmd(OWNCLOUD_CTL_BIN, "finish", login, password, protocol));
    }

    public String url(ConnectionPointProvider connectionPoint) {
        String json = ssh.run(connectionPoint, cmd(OWNCLOUD_CTL_BIN, "url"));
        try {
            return JSON.readValue(json, StringResult.class).data;
        } catch (IOException e) {
            String message = "Unable to parse ownCloud url response";
            logger.error(message+" "+json, e);
            throw new SyncloudException(message);
        }
    }

}
