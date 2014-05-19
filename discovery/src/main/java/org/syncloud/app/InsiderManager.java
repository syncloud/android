package org.syncloud.app;

import com.google.common.base.Optional;

import org.syncloud.model.InsiderConfig;
import org.syncloud.model.InsiderDnsConfig;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.parser.JsonParser;
import org.syncloud.ssh.Ssh;

import java.util.List;

import static java.util.Arrays.asList;

public class InsiderManager {

    private static final String INSIDER_BIN = "/opt/insider/bin/insider";

    public static Result<SshResult> addPort(String hostname, int port) {
        return Ssh.execute(hostname, asList(
                INSIDER_BIN + " add_port " + port,
                INSIDER_BIN + " cron_on "));

    }

    public static Result<SshResult> newName(String hostname, String email, String pass, String userDomain) {
        return Ssh.execute(hostname, asList(
                String.format("%s new_dns %s %s %s", INSIDER_BIN, userDomain, email, pass),
                INSIDER_BIN + " cron_on "));

    }

    public static Result<SshResult> activateExistingName(String hostname, String email, String pass) {
        return Ssh.execute(hostname, asList(
                String.format("%s existing_dns %s %s", INSIDER_BIN, email, pass),
                INSIDER_BIN + " cron_on "));

    }

    public static Result<Optional<PortMapping>> localPortMapping(String hostname, int localPort) {
        Result<List<PortMapping>> listResult = listPortMappings(hostname);
        if(listResult.hasError())
            return Result.error(listResult.getError());

        PortMapping found = null;
        for (PortMapping portMapping : listResult.getValue()) {
            if (portMapping.getLocal_port() == localPort)
                found = portMapping;
        }

        return Result.value(Optional.fromNullable(found));
    }

    public static Result<List<PortMapping>> listPortMappings(String hostname) {

        Result<SshResult> result = Ssh.execute(hostname, asList(INSIDER_BIN + " list_ports"));
        if (result.hasError())
            return Result.error(result.getError());

        return JsonParser.parse(result.getValue(), PortMapping.class);

    }

    public static Result<InsiderConfig> config(String hostname) {

        Result<SshResult> result = Ssh.execute(hostname, asList(INSIDER_BIN + " config"));
        if (result.hasError())
            return Result.error(result.getError());

        Result<List<InsiderConfig>> list = JsonParser.parse(result.getValue(), InsiderConfig.class);

        if (list.hasError())
            return Result.error(list.getError());

        if (list.getValue().size() != 1)
            return Result.error("unable to read configuration");

        return Result.value(list.getValue().get(0));

    }

    public static Result<Optional<InsiderDnsConfig>> dnsConfig(String hostname) {

        Result<SshResult> result = Ssh.execute(hostname, asList(INSIDER_BIN + " show_dns"));
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

    public static Result<SshResult> removePort(String hostname, int port) {
        return Ssh.execute(hostname, asList(INSIDER_BIN + " remove_port " + port));
    }
}
