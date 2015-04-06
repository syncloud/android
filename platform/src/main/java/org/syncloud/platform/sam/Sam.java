package org.syncloud.platform.sam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.common.Result;
import org.syncloud.common.SyncloudException;

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

    private <TContent> TContent runTyped(final TypeReference type, ConnectionPointProvider connectionPoint, String[] arguments) {
        String execute = ssh.run(connectionPoint, cmd(arguments));
        try {
            return JSON.<Result<TContent>>readValue(execute, type).data;
        } catch (IOException e) {
            String message = "Unable to parse command response";
            logger.error(message+" "+execute, e);
            throw new SyncloudException(message);
        }
    }

    private List<AppVersions> appsVersions(ConnectionPointProvider connectionPoint, String[] arguments) {
        return runTyped(new TypeReference<Result<List<AppVersions>>>() {}, connectionPoint, arguments);
    }

    public void run(ConnectionPointProvider connectionPoint, String... arguments) {
        ssh.run(connectionPoint, cmd(arguments));
    }

    public List<AppVersions> update(ConnectionPointProvider connectionPoint) {
        return appsVersions(connectionPoint, new String[] {Commands.update, "--release", release.getVersion()});
    }

    public List<AppVersions> list(ConnectionPointProvider connectionPoint) {
        return appsVersions(connectionPoint, new String[] {Commands.list});
    }
}
