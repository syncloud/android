package org.syncloud.owncloud;

import com.google.common.base.Optional;

import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.SshResult;

import static java.util.Arrays.asList;
import static org.syncloud.ssh.Ssh.execute;

public class OwncloudManager {

    private static String OWNCLOUD_CTL_BIN = "/opt/owncloud-ctl/bin/owncloud-ctl";

    public static Result<SshResult> finishSetup(Device device, String login, String password) {
        return execute(device, asList(String.format("%s finish %s %s", OWNCLOUD_CTL_BIN, login, password)));
    }

    public static Result<Optional<String>> owncloudUrl(Device device) {

        Result<SshResult> execute = execute(device, asList(String.format("%s url", OWNCLOUD_CTL_BIN)));
        if (execute.hasError())
            return Result.error(execute.getError());

        if (!execute.getValue().ok())
            return Result.value(Optional.<String>absent());
        else
            return Result.value(Optional.fromNullable(execute.getValue().getMessage()));

    }

}
