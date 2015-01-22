package org.syncloud.apps.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.StringResult;

import java.io.IOException;

import static java.lang.String.format;

public class InsiderManager {

    private static Logger logger = Logger.getLogger(InsiderManager.class);


    private static final String INSIDER_BIN = "insider";
    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;

    public InsiderManager() {
        this.ssh = new SshRunner();
    }

    public Optional<String> userDomain(ConnectionPointProvider connectionPoint) {
        Optional<String> execute = ssh.run(connectionPoint, format("%s user_domain", INSIDER_BIN));
        if (execute.isPresent()) {
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse user domain reply");
            }
        }

        logger.error("unable to get user domain reply");
        return Optional.absent();
    }

    public boolean dropDomain(ConnectionPointProvider connectionPoint) {
        Optional<String> execute = ssh.run(connectionPoint, format("%s drop_domain", INSIDER_BIN));
        if (!execute.isPresent())
            logger.error("unable to drop domain");
        return execute.isPresent();
    }
}
