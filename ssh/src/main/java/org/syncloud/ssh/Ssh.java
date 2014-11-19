package org.syncloud.ssh;

import org.syncloud.common.progress.Progress;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.common.model.Result;

public class Ssh {

    public static final int SSH_SERVER_PORT = 22;
    private SshRunner sshRunner;
    private EndpointSelector selector;

    private Progress progress;
    private EndpointPreference preference;

    public Ssh(SshRunner jsch, EndpointSelector selector, Progress progress, EndpointPreference preference) {
        this.sshRunner = jsch;
        this.selector = selector;
        this.progress = progress;
        this.preference = preference;
    }

    public Result<String> execute(final Device device, final String command) {

        progress.progress("ssh: " + command);
        Result<String> first = run(selector.first(device), device.credentials(), command, true);
        if (!first.hasError())
            return first;

        Result<String> result = run(selector.second(device), device.credentials(), command, false);
        if (!result.hasError())
            preference.swap();

        return result;

    }

    private Result<String> run(Result<Endpoint> endpoint, Credentials credentials, String command, boolean first) {

        if (endpoint.hasError()) {
            if (first)
                progress.progress(endpoint.getError());
            else
                progress.error(endpoint.getError());

            return Result.error(endpoint.getError());
        }

        try {
            return sshRunner.run(endpoint.getValue(), credentials, command);
        } catch (Exception e) {
            String message = "Endpoint is not available: " + e.getMessage();
            if (first)
                progress.progress(message);
            else
                progress.error(message);
        }

        return Result.error("unable to connect");

    }



}
