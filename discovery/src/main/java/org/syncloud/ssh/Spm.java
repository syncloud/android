package org.syncloud.ssh;


import org.syncloud.model.App;
import org.syncloud.model.Device;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.parser.JsonParser;

import java.util.List;

import static java.util.Arrays.asList;

public class Spm {

    public static final String REPO_URL = "https://raw.githubusercontent.com/syncloud/apps/master";
    public static final String UPDATE_REPO = "wget -qO- " + REPO_URL + "/spm | bash -s install";
    public static final String REPO_DIR = "/opt/syncloud/repo";
    public static final String SPM_BIN = REPO_DIR + "/system/spm";

    public static final String APP_INSIDER = "insider";
    public static final String APP_REMOTE_ACCESS = "remote_access";

    //TODO: We may get list of system tools from spm
    public static List<String> systemTools = asList(APP_INSIDER, APP_REMOTE_ACCESS);

    public static String SPM_APP_NAME = "spm";

    public enum Commnand {Install, Verify, Upgrade, Remove, Status}

    public static Result<SshResult> run(Commnand commnand, Device device, String app) {
        return Ssh.execute(device, asList(SPM_BIN + " " + commnand.name().toLowerCase() + " " + app));
    }

    public static Result<SshResult> installSpm(Device device) {
        Result<SshResult> result = Ssh.execute(device, asList(UPDATE_REPO));
        if (result.hasError())
            return result;

        return run(Commnand.Install, device, SPM_APP_NAME);
    }

    private static Result<SshResult> spmInstalled(Device device) {
        return Ssh.execute(device, asList("[ -d " + REPO_DIR + " ]"));
    }

    private static Result<Boolean> installed(Device device, String app) {
        Result<SshResult> result = run(Commnand.Status, device, app);
        if (result.hasError())
            return Result.error(result.getError());

        return Result.value(result.getValue().ok());
    }

    public static Result<SshResult> ensureSpmInstalled(Device device) {

        Result<SshResult> spmResult = spmInstalled(device);
        if (!spmResult.hasError() && !spmResult.getValue().ok()) {
            return installSpm(device);
        }
        return spmResult;

    }

    public static Result<Boolean> ensureSystemToolsInstalled(Device device) {

        Result<SshResult> result = ensureSpmInstalled(device);
        if (result.hasError())
            return Result.error(result.getError());

        for (String systemTool : systemTools) {
            Result<Boolean> installed = installed(device, systemTool);
            if (installed.hasError())
                return Result.error(installed.getError());

            if(installed.getValue())
                continue;

            Result<SshResult> install = run(Commnand.Install, device, systemTool);
            if (install.hasError())
                return Result.error(install.getError());
        }

        return Result.value(true);
    }

    public static Result<List<App>> list(Device device) {

        Result<SshResult> result = Ssh.execute(device, asList(SPM_BIN + " list"));
        if (result.hasError())
            return Result.error(result.getError());

        SshResult sshResult = result.getValue();
        if (!sshResult.ok()) {
            return Result.error(sshResult.getMessage());
        }

        return JsonParser.parse(sshResult, App.class);

    }
}
