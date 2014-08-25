package org.syncloud.remote;

import org.syncloud.common.model.Result;
import org.syncloud.insider.model.Endpoint;
import org.syncloud.ssh.Scp;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
import org.syncloud.ssh.model.ProxyEndpoint;

import static java.util.Arrays.asList;
import static org.syncloud.insider.InsiderManager.addService;
import static org.syncloud.insider.InsiderManager.removeService;
import static org.syncloud.insider.InsiderManager.serviceInfo;
import static org.syncloud.insider.InsiderManager.userDomain;
import static org.syncloud.ssh.Ssh.execute;

public class RemoteAccessManager {

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String KEY_FILE = "/root/.ssh/id_dsa_syncloud_master";
    public static final String SERVICE_NAME = "ssh";

    public static Result<String> disable(Device device) {
        return removeService(device, REMOTE_ACCESS_PORT);
    }

    public static Result<String> enable(final Device device) {

        Result<String> result = execute(device, asList(
                "mkdir -p /root/.ssh",
                String.format("rm -rf %s*", KEY_FILE),
                String.format("ssh-keygen -b 1024 -t dsa -f %s -N ''", KEY_FILE),
                String.format("cat %s.pub > /root/.ssh/authorized_keys", KEY_FILE)));

        return result.flatMap(new Result.Function<String, Result<String>>() {
            @Override
            public Result<String> apply(String input) throws Exception {
                return addService(device, SERVICE_NAME, "ssh", "_ssh._tcp", REMOTE_ACCESS_PORT, "ssh");
            }
        });
    }

    public static Result<Device> getRemoteDevice(final Device device, final String domain) {

        final DirectEndpoint directEndpoint = device.getLocalEndpoint();
        return Scp.getFile(directEndpoint, KEY_FILE)
                .flatMap(new Result.Function<String, Result<Device>>() {
                    @Override
                    public Result<Device> apply(final String key) throws Exception {
                        return userDomain(device)
                                .map(new Result.Function<String, Device>() {
                                    @Override
                                    public Device apply(String userDomain) throws Exception {
                                        return convert(userDomain + "." + domain, directEndpoint, key);
                                    }
                                });
                    }
                });

    }

    private static Device convert(String userDomain, DirectEndpoint directEndpoint, String key) {
        return new Device(
                null,
                null,
                userDomain,
                new DirectEndpoint(
                        directEndpoint.getHost(),
                        REMOTE_ACCESS_PORT,
                        "root", "syncloud", key)
        );
    }
}
