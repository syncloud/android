package org.syncloud.app;

import com.google.common.base.Optional;

import org.syncloud.model.Device;
import org.syncloud.model.InsiderResult;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.ssh.Scp;

import static java.util.Arrays.asList;
import static org.syncloud.app.InsiderManager.localPortMapping;
import static org.syncloud.model.Result.error;
import static org.syncloud.ssh.Ssh.execute;

public class RemoteAccessManager {

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String KEY_FILE = "/root/.ssh/id_dsa_syncloud_master";

    public static Result<Optional<Device>> getRemoteDevice(Device device) {
        Result<Optional<PortMapping>> result =
                localPortMapping(device, REMOTE_ACCESS_PORT);
        if (result.hasError())
            return error(result.getError());

        if (!result.getValue().isPresent())
            return Result.value(Optional.<Device>absent());

        Result<Device> remoteDevice = getRemote(device);
        if (remoteDevice.hasError())
            return error(remoteDevice.getError());

        return Result.value(Optional.fromNullable(remoteDevice.getValue()));
    }

    public static Result<Boolean> disable(Device device) {
        final Result<SshResult> result = InsiderManager.removePort(device, REMOTE_ACCESS_PORT);
        if (result.hasError())
            return error(result.getError());

        return Result.value(result.getValue().ok());

    }

    public static Result<Device> enable(Device device) {

        Result<SshResult> execute = execute(device, asList(
                "mkdir -p /root/.ssh",
                String.format("rm -rf %s*", KEY_FILE),
                String.format("ssh-keygen -b 1024 -t dsa -f %s -N ''", KEY_FILE),
                String.format("cat %s.pub > /root/.ssh/authorized_keys", KEY_FILE)));

        if (execute.hasError())
            return error(execute.getError());


        Result<SshResult> result = InsiderManager.addService(device, "ssh", "ssh", "_ssh._tcp", REMOTE_ACCESS_PORT, "ssh");
        if (result.hasError())
            return error(result.getError());

        return getRemote(device);
    }

    private static Result<Device> getRemote(Device device) {
        Result<String> key = Scp.getFile(device, KEY_FILE);
        if (key.hasError())
            return error(key.getError());

        Result<InsiderResult> fullNameResult = InsiderManager.fullName(device);
        if (fullNameResult.hasError()) {
            return error(fullNameResult.getError());
        }

        Result<Optional<PortMapping>> localPortMapping = localPortMapping(device, REMOTE_ACCESS_PORT);
        if (localPortMapping.hasError()) {
            return error(localPortMapping.getError());
        }

        Optional<PortMapping> localPortMappingValue = localPortMapping.getValue();
        if (!localPortMappingValue.isPresent()) {
            return error("unable to get external port");
        }

        return Result.value(new Device(
                fullNameResult.getValue().getData(),
                localPortMappingValue.get().getExternal_port(),
                key.getValue()));
    }
}
