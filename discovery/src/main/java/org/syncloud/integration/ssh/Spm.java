package org.syncloud.integration.ssh;


import org.syncloud.model.App;
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

    public static String SPM_APP_NAME = "spm";

    public enum Commnand {Install, Verify, Upgrade, Remove}

    public static Result<SshResult> run(Commnand commnand, String hostname, String app) {
        return Ssh.execute(hostname, asList(SPM_BIN + " " + commnand.name().toLowerCase() + " " + app));
    }

    public static Result<SshResult> installSpm(String hostname) {
        Result<SshResult> result = Ssh.execute(hostname, asList(UPDATE_REPO));
        if (result.hasError())
            return result;

        return run(Commnand.Install, hostname, SPM_APP_NAME);
    }

    private static Result<SshResult> spmInstalled(String hostname) {
        return Ssh.execute(hostname, asList("[ -d " + REPO_DIR + " ]"));
    }

    /*public static InstallStatus status(String hostname, String app) throws Exception {
//        ensureSpmInstalled(hostname);
        SshResult result = Ssh.execute(hostname, asList(SPM_BIN + " status " + app));
        return new InstallStatus(result.getExitCode() == 0, "", result.getMessage());
    }*/

    public static Result<SshResult> ensureSpmInstalled(String hostname) {

        Result<SshResult> spmResult = spmInstalled(hostname);
        if (!spmResult.hasError() && !spmResult.getValue().ok()) {
            return installSpm(hostname);
        }
        return spmResult;

    }

    /*public static VerifyStatus verify(String hostname, String app) throws Exception {
        SshResult result = Ssh.execute(hostname, asList(SPM_BIN + " verify " + app));
        return new VerifyStatus(result.getExitCode() == 0, result.getMessage());
    }*/

    public static Result<List<App>> list(String hostname) {

        Result<SshResult> result = Ssh.execute(hostname, asList(SPM_BIN + " list"));
        if (result.hasError())
            return Result.error(result.getError());

        SshResult sshResult = result.getValue();
        if (!sshResult.ok()) {
            return Result.error(sshResult.getMessage());
        }

        return JsonParser.parse(sshResult, App.class);

    }
}
