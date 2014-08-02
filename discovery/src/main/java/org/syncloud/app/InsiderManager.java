package org.syncloud.app;

import com.google.common.base.Optional;

import org.syncloud.model.Device;
import org.syncloud.model.InsiderConfig;
import org.syncloud.model.InsiderDnsConfig;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.parser.JsonParser;
import org.syncloud.ssh.Ssh;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.syncloud.ssh.Ssh.execute;

public class InsiderManager {

    private static final String INSIDER_BIN = "insidercli";

    public static Result<SshResult> addService(Device device, String name, String protocol, String type, int port, String url) {
        Result<SshResult> result = execute(device, asList(
                format("%s add_service %s %s %s %s %s", INSIDER_BIN, name, protocol, type, port, url)));
        if (result.hasError())
            return result;

        if(!result.getValue().ok())
            return Result.error(result.getValue().getMessage());

        return enableCron(device);

    }

    private static Result<SshResult> enableCron(Device device) {
        Result<SshResult> result = execute(device, asList(INSIDER_BIN + " cron_on "));
        if (!result.hasError() && !result.getValue().ok())
            return Result.error(result.getValue().getMessage());

        return result;
    }

    /*public static Result<SshResult> newName(Device device, String email, String pass, String userDomain) {

        Result<SshResult> result = Ssh.execute(device, asList(String.format("%s new_dns %s %s %s", INSIDER_BIN, userDomain, email, pass)));
        if (result.hasError())
            return result;

        if(!result.getValue().ok())
            return Result.error(result.getValue().getMessage());

        return enableCron(device);

    }*/

    public static Result<SshResult> acquireDomain(
            Device device, String email, String pass, String domain) {

        Result<SshResult> result = execute(
                device,
                asList(format("%s acquire_domain %s %s %s", INSIDER_BIN, email, pass, domain)));
        if (result.hasError())
            return result;

        if(!result.getValue().ok())
            return Result.error(result.getValue().getMessage());

        return enableCron(device);

    }

    public static Result<Optional<PortMapping>> localPortMapping(Device device, int localPort) {
        Result<List<PortMapping>> listResult = listPortMappings(device);
        if(listResult.hasError())
            return Result.error(listResult.getError());

        PortMapping found = null;
        for (PortMapping portMapping : listResult.getValue()) {
            if (portMapping.getLocal_port() == localPort)
                found = portMapping;
        }

        return Result.value(Optional.fromNullable(found));
    }

    public static Result<List<PortMapping>> listPortMappings(Device device) {

        Result<SshResult> result = execute(device, asList(INSIDER_BIN + " list_ports"));
        if (result.hasError())
            return Result.error(result.getError());

        return JsonParser.parse(result.getValue(), PortMapping.class);

    }

    public static Result<InsiderConfig> config(Device device) {

        Result<SshResult> result = execute(device, asList(INSIDER_BIN + " config"));
        if (result.hasError())
            return Result.error(result.getError());

        Result<List<InsiderConfig>> list = JsonParser.parse(result.getValue(), InsiderConfig.class);

        if (list.hasError())
            return Result.error(list.getError());

        if (list.getValue().size() != 1)
            return Result.error("unable to read configuration");

        return Result.value(list.getValue().get(0));

    }

    public static Result<Optional<InsiderDnsConfig>> dnsConfig(Device device) {

        Result<SshResult> result = execute(device, asList(INSIDER_BIN + " show_dns"));
        if (result.hasError())
            return Result.error(result.getError());

        Result<List<InsiderDnsConfig>> list = JsonParser.parse(result.getValue(), InsiderDnsConfig.class);

        if (list.hasError())
            return Result.error(list.getError());

        if (list.getValue().size() != 1)
            return Result.value(Optional.<InsiderDnsConfig>absent());
        else
            return Result.value(Optional.fromNullable(list.getValue().get(0)));

    }

    public static Result<SshResult> removePort(Device device, int port) {
        return execute(device, asList(INSIDER_BIN + " remove_port " + port));
    }
}
