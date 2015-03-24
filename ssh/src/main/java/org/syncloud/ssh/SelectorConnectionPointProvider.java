package org.syncloud.ssh;

import com.google.common.base.Optional;

import org.syncloud.ssh.model.ConnectionPoint;
import org.syncloud.ssh.model.Device;

import static org.syncloud.ssh.SshRunner.cmd;


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
        Optional<ConnectionPoint> firstPoint = selector.select(device, true);
        if (firstPoint.isPresent()){
            if (sshRunner.run(firstPoint.get(), cmd(VERIFY_COMMAND)).isPresent())
                return firstPoint.get();
        }

        Optional<ConnectionPoint> secondPoint = selector.select(device, false);
        if (secondPoint.isPresent()) {
            if (sshRunner.run(secondPoint.get(), cmd(VERIFY_COMMAND)).isPresent()) {
                preference.swap();
                return secondPoint.get();
            }
        }

        throw new RuntimeException("Can't run command, unable to get working connection point");
    }
}
