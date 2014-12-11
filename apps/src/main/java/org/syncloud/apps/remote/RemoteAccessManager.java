package org.syncloud.apps.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.StringResult;

public class RemoteAccessManager {
    private static Logger logger = Logger.getLogger(RemoteAccessManager.class);

    public static final ObjectMapper JSON = new ObjectMapper();

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String REMOTE_BIN = "remote";
    private InsiderManager insider;
    private Ssh ssh;

    public RemoteAccessManager(InsiderManager insider, Ssh ssh) {
        this.insider = insider;
        this.ssh = ssh;
    }

    public Optional<Device> enable(final Device device, final String domain) {
        final Endpoint endpoint = device.localEndpoint();
        Optional<String> execute = ssh.execute(device, REMOTE_BIN + " enable");
        if (execute.isPresent()) {
            try {
                final String key = JSON.readValue(execute.get(), StringResult.class).data;
                Optional<String> userDomain = insider.userDomain(device);
                if (userDomain.isPresent()) {
                    return Optional.of(new Device(
                            device.macAddress(),
                            device.id(),
                            userDomain.get() + "." + domain,
                            new Endpoint(endpoint.host(), REMOTE_ACCESS_PORT),
                            new Credentials("root", "syncloud", key)));

                }
                logger.error("unable to get user domain");
            } catch (Exception e) {
                logger.error("unable to remote access app ssh response");
            }
        }

        return Optional.absent();

    }

}