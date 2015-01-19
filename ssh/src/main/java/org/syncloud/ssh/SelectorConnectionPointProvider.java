package org.syncloud.ssh;

import com.google.common.base.Optional;

import org.syncloud.ssh.model.ConnectionPoint;
import org.syncloud.ssh.model.Device;

public class SelectorConnectionPointProvider implements ConnectionPointProvider {
    public static final String VERIFY_COMMAND = "date";

    private SshRunner sshRunner;
    private EndpointSelector selector;
    private EndpointPreference preference;
    private Device device;

    public SelectorConnectionPointProvider(SshRunner sshRunner, EndpointSelector selector, EndpointPreference preference, Device device) {
        this.selector = selector;
        this.sshRunner = sshRunner;
        this.preference = preference;
        this.device = device;
    }

    @Override
    public ConnectionPoint get() {
        Optional<ConnectionPoint> firstEndpoint = selector.select(device, true);
        if (firstEndpoint.isPresent()){
            if (sshRunner.run(firstEndpoint.get(), VERIFY_COMMAND).isPresent())
                return firstEndpoint.get();
        }

        Optional<ConnectionPoint> secondEndpoint = selector.select(device, false);
        if (secondEndpoint.isPresent()) {
            if (sshRunner.run(secondEndpoint.get(), VERIFY_COMMAND).isPresent()) {
                preference.swap();
                return secondEndpoint.get();
            }
        }

        return null;
    }
}
