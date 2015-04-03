package org.syncloud.platform.ssh;

import org.syncloud.platform.ssh.model.ConnectionPoint;
import org.syncloud.platform.ssh.model.Device;
import org.syncloud.platform.ssh.model.SyncloudException;

import static org.syncloud.platform.ssh.SshRunner.cmd;


public class SelectorConnectionPointProvider implements ConnectionPointProvider {
    public static final String VERIFY_COMMAND = "syncloud-ping";

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
        ConnectionPoint firstPoint = selector.select(device, true);
        try {
            sshRunner.run(firstPoint, cmd(VERIFY_COMMAND));
            return firstPoint;
        } catch (Throwable th) {}

        ConnectionPoint secondPoint = selector.select(device, false);
        try {
            sshRunner.run(secondPoint, cmd(VERIFY_COMMAND));
            return secondPoint;
        } catch (Throwable th) {}

        throw new SyncloudException("Can't run command, unable to get working connection point");
    }
}
