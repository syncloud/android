package org.syncloud.integration.ssh;

import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.parser.JsonParser;

import java.util.List;

import static java.util.Arrays.asList;

public class Insider {

    private static final String INSIDER_BIN = "/opt/insider/bin/insider";

    public static Result<SshResult> addPort(String hostname, int port) {
        return Ssh.execute(hostname, asList(
                INSIDER_BIN + " add_port " + port,
                INSIDER_BIN + " cron_on "));

    }

    public static Result<List<PortMapping>> listPortMappings(String hostname) {

        Result<SshResult> result = Ssh.execute(hostname, asList(INSIDER_BIN + " list_ports"));
        if (result.hasError())
            return Result.error(result.getError());

        return JsonParser.parse(result.getValue(), PortMapping.class);

    }

    public static Result<SshResult> removePort(String hostname, int port) {
        return Ssh.execute(hostname, asList(INSIDER_BIN + " remove_port " + port));
    }
}
