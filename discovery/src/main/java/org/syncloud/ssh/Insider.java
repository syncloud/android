package org.syncloud.ssh;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Insider {

    private static final String INSIDER_BIN = "/opt/insider/bin/insider";
    public static final String PORT_LIST_PREFIX = "port mapping: ";

    public static Result<SshResult> addPort(String hostname, int port) {
        try {
            return Result.value(Ssh.execute(hostname, asList(
                    INSIDER_BIN + " add_port " + port,
                    INSIDER_BIN + " cron_on "
            )));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public static Result<List<PortMapping>> listPortMappings(String hostname) {
        try {
            SshResult result = Ssh.execute(hostname, asList(INSIDER_BIN + " list_ports | grep '" + PORT_LIST_PREFIX + "'"));
            ObjectMapper mapper = new ObjectMapper();

            List<PortMapping> mappings = new ArrayList<PortMapping>();
            if (result.ok()) {
                String[] lines = result.getMessage().split("\\r?\\n");
                for (String line : lines) {
                    String json = line.replace(PORT_LIST_PREFIX, "");
                    mappings.add(mapper.readValue(json, PortMapping.class));
                }
            }
            return Result.value(mappings);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public static Result<SshResult> removePort(String hostname, int port) {
        try {
            return Result.value(Ssh.execute(hostname, asList(INSIDER_BIN + " remove_port " + port)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
