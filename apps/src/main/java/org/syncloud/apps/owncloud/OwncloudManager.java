package org.syncloud.apps.owncloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.StringResult;

import java.io.IOException;

public class OwncloudManager {

    private static Logger logger = Logger.getLogger(OwncloudManager.class);

    private static String OWNCLOUD_CTL_BIN = "owncloud-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();
    private Ssh ssh;

    public OwncloudManager(Ssh ssh) {
        this.ssh = ssh;
    }

    public Optional<String> finishSetup(Device device, String login, String password, String protocol) {
        return ssh.execute(device, String.format("%s finish %s %s %s", OWNCLOUD_CTL_BIN, login, password, protocol));
    }

    public Optional<String> owncloudUrl(Device device) {

        Optional<String> execute = ssh.execute(device, String.format("%s url", OWNCLOUD_CTL_BIN));
        if (execute.isPresent())
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse ownCloud url response");
            }

        return Optional.absent();

    }

}
