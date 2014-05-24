package org.syncloud.ssh;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

    public static String SPM_APP_NAME = "spm";

    public enum Command {Install, Verify, Upgrade, Remove, Status}

    public static Result<SshResult> run(Command command, Device device, String app) {
        return Ssh.execute(device, asList(SPM_BIN + " " + command.name().toLowerCase() + " " + app));
    }

    public static Result<SshResult> installSpm(Device device) {
        return Ssh.execute(device, asList(UPDATE_REPO));
    }

    private static Result<SshResult> spmInstalled(Device device) {
        return Ssh.execute(device, asList("[ -d " + REPO_DIR + " ]"));
    }

    public static Result<SshResult> ensureSpmInstalled(Device device) {

        Result<SshResult> spmResult = spmInstalled(device);
        if (!spmResult.hasError() && !spmResult.getValue().ok()) {
            return installSpm(device);
        }
        return spmResult;

    }

    public static Result<Boolean> ensureAdminToolsInstalled(Device device, Function<String, String> progress) {

        progress.apply("installing spm");
        Result<SshResult> result = installSpm(device);
        if (result.hasError())
            return Result.error(result.getError());

        progress.apply("getting list of apps");
        Result<List<App>> list = list(device);
        if (list.hasError())
            return Result.error(list.getError());

        for (App app : filter(list.getValue(), App.Type.admin)) {

            Command command;
            if(!app.getInstalled()) {
                command = Command.Install;
            } else {
                if (!app.getVersion().equals(app.getInstalledVersion()))
                    command = Command.Upgrade;
                else
                    continue;
            }

            progress.apply("installing " + app.getName());
            Result<SshResult> install = run(command, device, app.getId());
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

        Result<List<App>> parse = JsonParser.parse(sshResult, App.class);
        if (!parse.hasError()) {
            return Result.value(filterNot(parse.getValue(), App.Type.system));
        }
        return Result.error(parse.getError());

    }

    private static List<App> filterNot(List<App> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<App>() {
            @Override
            public boolean apply(App input) {
                return input.getAppType() != type;
            }
        }));
    }

    private static List<App> filter(List<App> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<App>() {
            @Override
            public boolean apply(App input) {
                return input.getAppType() == type;
            }
        }));
    }
}
