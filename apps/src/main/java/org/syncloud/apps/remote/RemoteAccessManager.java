package org.syncloud.apps.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.StringResult;

public class RemoteAccessManager {
    private static Logger logger = Logger.getLogger(RemoteAccessManager.class);

    public static final ObjectMapper JSON = new ObjectMapper();

    private static final String REMOTE_BIN = "remote";
    private InsiderManager insider;
    private SshRunner ssh;

    public RemoteAccessManager(InsiderManager insider) {
        this.insider = insider;
        this.ssh = new SshRunner();
    }

    public Optional<Credentials> enable(ConnectionPointProvider connectionPoint, final String domain) {
        Optional<String> execute = ssh.run(connectionPoint, REMOTE_BIN + " enable");
        if (execute.isPresent()) {
            try {
                final String key = JSON.readValue(execute.get(), StringResult.class).data;
                Optional<String> userDomain = insider.userDomain(connectionPoint);
                if (userDomain.isPresent()) {
                    return Optional.of(new Credentials("root", "syncloud", key));

                }
                logger.error("unable to get user domain");
            } catch (Exception e) {
                logger.error("unable to remote access app ssh response");
            }
        }

        return Optional.absent();
    }
}