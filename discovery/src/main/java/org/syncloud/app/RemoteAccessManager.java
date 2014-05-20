package org.syncloud.app;

import com.google.common.base.Optional;

import org.syncloud.model.Device;
import org.syncloud.model.InsiderConfig;
import org.syncloud.model.InsiderDnsConfig;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.ssh.Scp;
import org.syncloud.ssh.Ssh;

import static java.util.Arrays.asList;

public class RemoteAccessManager {

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String KEY_FILE = "/root/.ssh/id_dsa_syncloud_master";

    public static Result<Optional<Device>> getRemoteDevice(Device device) {
        Result<Optional<PortMapping>> result = InsiderManager
                .localPortMapping(device, REMOTE_ACCESS_PORT);
        if (result.hasError())
            return Result.error(result.getError());

        if (!result.getValue().isPresent())
            return Result.value(Optional.<Device>absent());

        Result<Device> remoteDevice = getRemote(device);
        if (remoteDevice.hasError())
            return Result.error(remoteDevice.getError());

        return Result.value(Optional.fromNullable(remoteDevice.getValue()));
    }

    public static Result<Boolean> disable(Device device) {
        final Result<SshResult> result = InsiderManager.removePort(device, REMOTE_ACCESS_PORT);
        if (result.hasError())
            return Result.error(result.getError());

        return Result.value(result.getValue().ok());

    }

    public static Result<Device> enable(Device device) {



        Result<SshResult> execute = Ssh.execute(device, asList(
                "mkdir -p /root/.ssh",
                String.format("rm -rf %s*", KEY_FILE),
                String.format("ssh-keygen -b 1024 -t dsa -f %s -N ''", KEY_FILE),
                String.format("cat %s.pub > /root/.ssh/authorized_keys", KEY_FILE)));

        if (execute.hasError())
            return Result.error(execute.getError());


        Result<SshResult> result = InsiderManager.addPort(device, REMOTE_ACCESS_PORT);
        if (result.hasError())
            return Result.error(result.getError());

        return getRemote(device);
    }

    private static Result<Device> getRemote(Device device) {
        Result<String> key = Scp.getFile(device, KEY_FILE);
        if (key.hasError())
            return Result.error(key.getError());

        Result<Optional<InsiderDnsConfig>> dnsResult = InsiderManager.dnsConfig(device);
        if (dnsResult.hasError()) {
            return Result.error(dnsResult.getError());
        }

        Result<InsiderConfig> configResult = InsiderManager.config(device);
        if (configResult.hasError()) {
            return Result.error(configResult.getError());
        }

        Optional<InsiderDnsConfig> dns = dnsResult.getValue();
        if (!dns.isPresent()) {
            return Result.error("unable to get public name for the device");
        }

        Result<Optional<PortMapping>> localPortMapping = InsiderManager.localPortMapping(device, REMOTE_ACCESS_PORT);
        if (localPortMapping.hasError()) {
            return Result.error(localPortMapping.getError());
        }

        Optional<PortMapping> localPortMappingValue = localPortMapping.getValue();
        if (!localPortMappingValue.isPresent()) {
            return Result.error("unable to get external port");
        }

        return Result.value(new Device(
                "device." + dns.get().getUser_domain() + "." + configResult.getValue().getDomain(),
                localPortMappingValue.get().getExternal_port(),
                key.getValue()));
    }
}
