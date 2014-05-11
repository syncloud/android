package org.syncloud.ssh;


import org.syncloud.model.InstallStatus;
import org.syncloud.model.SshResult;
import org.syncloud.model.VerifyStatus;

import static java.util.Arrays.asList;

public class Spm {

    public static final String REPO_URL = "https://raw.githubusercontent.com/syncloud/apps/master";
    public static final String UPDATE_REPO = "wget -qO- " + REPO_URL + "/system/download_repo.sh | sh";
    public static final String REPO_DIR = "/opt/syncloud/repo";
    public static final String INSTALLER = REPO_DIR + "/system/spm";

    public static String install(String hostname, String app) throws Exception {

        SshResult result = Ssh.execute(hostname, asList(
                UPDATE_REPO,
                INSTALLER + " install " + app
        ));
        return result.getMessage();
    }

    private static SshResult installSpm(String hostname) throws Exception {
        return Ssh.execute(hostname, asList(UPDATE_REPO));
    }

    private static SshResult spmInstalled(String hostname) throws Exception {
        return Ssh.execute(hostname, asList("[ -d " + REPO_DIR + " ]"));
    }

    public static InstallStatus status(String hostname, String app) throws Exception {
        ensureSpmInstalled(hostname);
        SshResult result = Ssh.execute(hostname, asList(INSTALLER + " status " + app));
        return new InstallStatus(result.getExitCode() == 0, "", result.getMessage());
    }

    private static void ensureSpmInstalled(String hostname) throws Exception {
        SshResult spmInstalled = spmInstalled(hostname);
        if (!spmInstalled.ok())
            installSpm(hostname);
    }

    public static VerifyStatus verify(String hostname, String app) throws Exception {
        SshResult result = Ssh.execute(hostname, asList(INSTALLER + " verify " + app));
        return new VerifyStatus(result.getExitCode() == 0, result.getMessage());
    }

    public static String remove(String hostname, String app) throws Exception {

        SshResult result = Ssh.execute(hostname, asList(INSTALLER + " remove " + app));
        return result.getMessage();
    }
}
