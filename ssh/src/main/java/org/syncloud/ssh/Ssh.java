package org.syncloud.ssh;

import org.apache.log4j.Logger;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.common.model.Result;

public class Ssh {

    private static Logger logger = Logger.getLogger(Ssh.class);

    public static final int SSH_SERVER_PORT = 22;
    private SshRunner sshRunner;
    private EndpointSelector selector;
    private EndpointPreference preference;

    public Ssh(SshRunner jsch, EndpointSelector selector, EndpointPreference preference) {
        this.sshRunner = jsch;
        this.selector = selector;
        this.preference = preference;
    }

    public Result<String> execute(final Device device, final String command) {

        logger.info(command);

        Result<String> first = run(selector.first(device), device.credentials(), command);
        if (!first.hasError())
            return first;

        Result<String> second = run(selector.second(device), device.credentials(), command);
        if (!second.hasError())
            preference.swap();

        return second;

    }

    private Result<String> run(Result<Endpoint> endpoint, Credentials credentials, String command) {

        if (endpoint.hasError()) {
            logger.error(endpoint.getError());
            return Result.error(endpoint.getError());
        }

        try {
            return sshRunner.run(endpoint.getValue(), credentials, command);
        } catch (Exception e) {
            logger.error("Endpoint is not available: " + e.getMessage());
        }

        return Result.error("unable to connect");

    }


}
