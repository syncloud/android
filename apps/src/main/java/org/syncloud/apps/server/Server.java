package org.syncloud.apps.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.SshResult;

import java.io.IOException;

import static java.lang.String.format;

public class Server {

    private static Logger logger = Logger.getLogger(Server.class);

    public static final ObjectMapper JSON = new ObjectMapper();


    private SshRunner ssh;

    public Server(SshRunner ssh) {
        this.ssh = ssh;
    }

    public Optional<Credentials> activate(
            ConnectionPointProvider connectionPoint,
            String version,
            String topLevelDomain,
            String apiUrl,
            String email,
            String pass,
            String userDomain) {

        logger.info("activating " + userDomain);

        Optional<String> run = ssh.run(connectionPoint,
                format("syncloud-cli activate %s %s %s %s %s %s",
                        version, topLevelDomain, apiUrl, email, pass, userDomain));

        if (run.isPresent()) {
            try {
                SshResult<Credentials> reference = JSON.readValue(run.get(), new TypeReference<SshResult<Credentials>>() {});
                return Optional.of(reference.data);
            } catch (IOException e) {
                logger.error("unable to parse execute response", e);
            }
        } else {
            logger.error("unable to execute command");
        }

        return Optional.absent();
    }
}
