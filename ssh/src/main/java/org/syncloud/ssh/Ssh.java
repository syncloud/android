package org.syncloud.ssh;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

public class Ssh {

    public static final String VERIFY_COMMAND = "date";
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

    public Optional<String> execute(final Device device, final String command) {
        Optional<Endpoint> selected = select(device);
        if(selected.isPresent()) {
            return sshRunner.run(selected.get(), device.credentials(), command);
        }
        logger.error("unable to connect");
        return Optional.absent();

    }

    private Optional<Endpoint> select(final Device device) {
        Optional<Endpoint> firstEndpoint = selector.select(device, true);
        if (firstEndpoint.isPresent()){
            if (sshRunner.run(firstEndpoint.get(), device.credentials(), VERIFY_COMMAND).isPresent())
                return firstEndpoint;
        }

        Optional<Endpoint> secondEndpoint = selector.select(device, false);
        if (secondEndpoint.isPresent()) {
            if (sshRunner.run(secondEndpoint.get(), device.credentials(), VERIFY_COMMAND).isPresent()) {
                preference.swap();
                return secondEndpoint;
            }
        }

        return Optional.absent();
    }


}
