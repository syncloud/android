package org.syncloud.remote;

import org.syncloud.common.model.Results;
import org.syncloud.insider.InsiderManager;
import org.syncloud.insider.model.Endpoint;
import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.DeviceEndpoint;
import org.syncloud.ssh.Scp;

import static java.util.Arrays.asList;
import static org.syncloud.common.model.Result.error;
import static org.syncloud.common.model.Results.flatten;
import static org.syncloud.insider.InsiderManager.removeService;
import static org.syncloud.ssh.Ssh.execute1;

public class RemoteAccessManager {

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String KEY_FILE = "/root/.ssh/id_dsa_syncloud_master";
    public static final String SERVICE_NAME = "ssh";

    public static Result<String> disable(Device device) {
        return removeService(device, REMOTE_ACCESS_PORT);
    }

    public static Result<Device> enable(final Device device) {

        Result<String> execute = execute1(device, asList(
                "mkdir -p /root/.ssh",
                String.format("rm -rf %s*", KEY_FILE),
                String.format("ssh-keygen -b 1024 -t dsa -f %s -N ''", KEY_FILE),
                String.format("cat %s.pub > /root/.ssh/authorized_keys", KEY_FILE)));

        if (execute.hasError())
            return error(execute.getError());

        Result<String> result = InsiderManager.addService(
                device, SERVICE_NAME, "ssh", "_ssh._tcp", REMOTE_ACCESS_PORT, "");
        return flatten(result.map(new Result.Function<String, Result<Device>>() {
            @Override
            public Result<Device> apply(String input) throws Exception {
                return getRemote(device);
            }
        }));
    }

    public static Result<Device> getRemote(final Device device) {
        Result<String> key = Scp.getFile(device, KEY_FILE);
        if (key.hasError())
            return error(key.getError());

        Result<Endpoint> endpoint = InsiderManager.serviceInfo(device, SERVICE_NAME);
        return endpoint.map(new Result.Function<Endpoint, Device>() {
            @Override
            public Device apply(Endpoint input) throws Exception {
                return convert(input, device);
            }
        });
    }

    private static Device convert(Endpoint input, Device device) {
        return new Device(
                new DeviceEndpoint(
                        input.getExternal_host(),
                        input.getExternal_port()),
                new DeviceEndpoint(
                        device.getLocalEndpoint().getHost(),
                        input.getService().getPort()));
    }
}
