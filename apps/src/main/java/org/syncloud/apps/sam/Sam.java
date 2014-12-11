package org.syncloud.apps.sam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.SshResult;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.syncloud.common.model.Result.error;
import static org.syncloud.common.model.Result.value;

public class Sam {
    private static Logger logger = Logger.getLogger(Sam.class);

    public static final ObjectMapper JSON = new ObjectMapper();
    private Ssh ssh;
    private Release release;

    public Sam(Ssh ssh, Release release) {
        this.ssh = ssh;
        this.release = release;
    }

    public static String cmd(String... arguments) {
        List<String> cmd = asList(arguments);
        return "sam "+ join(cmd, " ");
    }

    private <TContent> Optional<TContent> runTyped(final TypeReference type, Device device, String... arguments) {
        Optional<String> execute = ssh.execute(device, cmd(arguments));
        if (execute.isPresent()) {
            try {
                return Optional.of(JSON.<SshResult<TContent>>readValue(execute.get(), type).data);
            } catch (IOException e) {
                logger.error("unable to parse command response");
                return Optional.absent();
            }
        }
        logger.error("unable to execute command");
        return Optional.absent();
    }

    private Optional<List<AppVersions>> appsVersions(Device device, String... arguments) {
        return runTyped(new TypeReference<SshResult<List<AppVersions>>>() {}, device, arguments);
    }

    public Result<String> run(Device device, String... arguments) {
        Optional<String> execute = ssh.execute(device, cmd(arguments));
        if (execute.isPresent()) {

            try {
                SshResult result = JSON.readValue(execute.get(), SshResult.class);
                if (result.success)
                    return value(result.message);
                else
                    return error(result.message);

            } catch (IOException e) {
                return error("unable to parse execute response");
            }
        }

        return error("unable to execute command");
    }

    public Optional<List<AppVersions>> update(Device device) {
        return appsVersions(device, Commands.update, "--release", release.getVersion());
    }

    public Optional<List<AppVersions>> list(Device device) {
        return appsVersions(device, Commands.list);
    }
}
