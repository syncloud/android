package org.syncloud.apps.remote;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.StringResult;

public class RemoteAccessManager {
    public static final ObjectMapper JSON = new ObjectMapper();

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String REMOTE_BIN = "remote";
    private InsiderManager insider;
    private Ssh ssh;

    public RemoteAccessManager(InsiderManager insider, Ssh ssh) {
        this.insider = insider;
        this.ssh = ssh;
    }

    public Result<String> disable(Device device) {
        return ssh.execute(device, REMOTE_BIN + " disable");
    }

    public Result<Device> enable(final Device device, final String domain) {
        final Endpoint endpoint = device.localEndpoint();
        return ssh.execute(device, REMOTE_BIN + " enable")
                .flatMap(new Result.Function<String, Result<Device>>() {
                    @Override
                    public Result<Device> apply(final String data) throws Exception {
                        final String key = JSON.readValue(data, StringResult.class).data;
                        return insider.userDomain(device)
                                .map(new Result.Function<String, Device>() {
                                    @Override
                                    public Device apply(String userDomain) throws Exception {
                                        Endpoint remoteEndpoint = new Endpoint(endpoint.host(), REMOTE_ACCESS_PORT);
                                        Credentials credentials = new Credentials("root", "syncloud", key);
                                        Device newDevice = new Device(
                                                device.macAddress(),
                                                device.id(),
                                                userDomain + "." + domain,
                                                remoteEndpoint,
                                                credentials);
                                        return newDevice;
                                    }
                                });
                    }
                });
    }

}