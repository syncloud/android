package org.syncloud.apps.owncloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.StringResult;

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

    public Optional<String> finishSetup(ConnectionPointProvider connectionPoint, String login, String password, String protocol) {
        return ssh.run(connectionPoint, cmd(OWNCLOUD_CTL_BIN, "finish", login, password, protocol));
    }

    public Optional<String> owncloudUrl(ConnectionPointProvider connectionPoint) {

        Optional<String> execute = ssh.run(connectionPoint, cmd(OWNCLOUD_CTL_BIN, "url"));
        if (execute.isPresent())
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse ownCloud url response");
            }

        return Optional.absent();

    }

}
