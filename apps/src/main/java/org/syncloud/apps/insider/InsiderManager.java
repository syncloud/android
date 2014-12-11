package org.syncloud.apps.insider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.StringResult;

import java.io.IOException;

import static java.lang.String.format;

public class InsiderManager {

    private static Logger logger = Logger.getLogger(InsiderManager.class);


    private static final String INSIDER_BIN = "insider";
    public static final ObjectMapper JSON = new ObjectMapper();
    private Ssh ssh;

    public InsiderManager(Ssh ssh) {
        this.ssh = ssh;
    }

    public Optional<String> userDomain(Device device) {
        Optional<String> execute = ssh.execute(device, format("%s user_domain", INSIDER_BIN));
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

    public boolean acquireDomain(final Device device, String email, String pass, String domain) {
        return ssh.execute(device, format("%s acquire_domain %s %s %s", INSIDER_BIN, email, pass, domain)).isPresent();
    }

    public boolean setRedirectInfo(Device device, String domain, String apiUl) {
        return ssh.execute(device, format("%s set_redirect_info %s %s", INSIDER_BIN, domain, apiUl)).isPresent();
    }

    public boolean dropDomain(Device device) {
        Optional<String> execute = ssh.execute(device, format("%s drop_domain", INSIDER_BIN));
        if (!execute.isPresent())
            logger.error("unable to drop domain");
        return execute.isPresent();
    }
}
