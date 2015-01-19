package org.syncloud.apps.owncloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.StringResult;

import java.io.IOException;

public class OwncloudManager {

    private static Logger logger = Logger.getLogger(OwncloudManager.class);

    private static String OWNCLOUD_CTL_BIN = "owncloud-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;

    public OwncloudManager() {
        this.ssh = new SshRunner();
    }

    public Optional<String> finishSetup(ConnectionPointProvider connectionPoint, String login, String password, String protocol) {
        return ssh.run(connectionPoint, String.format("%s finish %s %s %s", OWNCLOUD_CTL_BIN, login, password, protocol));
    }

    public Optional<String> owncloudUrl(ConnectionPointProvider connectionPoint) {

        Optional<String> execute = ssh.run(connectionPoint, String.format("%s url", OWNCLOUD_CTL_BIN));
        if (execute.isPresent())
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse ownCloud url response");
            }

        return Optional.absent();

    }

}
