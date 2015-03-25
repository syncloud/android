package org.syncloud.platform.sam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.SshResult;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Sam {
    private static Logger logger = Logger.getLogger(Sam.class);

    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;
    private Release release;

    public Sam(SshRunner sshRunner, Release release) {
        this.ssh = sshRunner;
        this.release = release;
    }

    public static String[] cmd(String[] arguments) {
        List<String> allArguments = newArrayList(arguments);
        allArguments.add(0, "sam");
        return allArguments.toArray(new String[] {});
    }

    private <TContent> Optional<TContent> runTyped(final TypeReference type, ConnectionPointProvider connectionPoint, String[] arguments) {
        Optional<String> execute = ssh.run(connectionPoint, cmd(arguments));
        if (execute.isPresent()) {
            try {
                return Optional.of(JSON.<SshResult<TContent>>readValue(execute.get(), type).data);
            } catch (IOException e) {
                logger.error("unable to parse command response", e);
                return Optional.absent();
            }
        }
        logger.error("unable to execute command");
        return Optional.absent();
    }

    private Optional<List<AppVersions>> appsVersions(ConnectionPointProvider connectionPoint, String[] arguments) {
        return runTyped(new TypeReference<SshResult<List<AppVersions>>>() {}, connectionPoint, arguments);
    }

    public Boolean run(ConnectionPointProvider connectionPoint, String... arguments) {
        Optional<String> execute = ssh.run(connectionPoint, cmd(arguments));
        if (execute.isPresent()) {

            try {
                SshResult result = JSON.readValue(execute.get(), SshResult.class);
                if (result.success)
                    return true;

                logger.error(result.message);

            } catch (IOException e) {
                logger.error("unable to parse execute response");
            }
        } else {
            logger.error("unable to execute command");
        }

        return false;
    }

    public Optional<List<AppVersions>> update(ConnectionPointProvider connectionPoint) {
        return appsVersions(connectionPoint, new String[] {Commands.update, "--release", release.getVersion()});
    }

    public Optional<List<AppVersions>> list(ConnectionPointProvider connectionPoint) {
        return appsVersions(connectionPoint, new String[] {Commands.list});
    }
}
