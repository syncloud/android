package org.syncloud.apps.spm;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;

import java.util.List;

public class Spm {
    public static final ObjectMapper JSON = new ObjectMapper();

    public static final String REPO_URL = "https://raw.githubusercontent.com/syncloud/apps/release";
    public static final String INSTALL_SPM = "wget -qO- " + REPO_URL + "/spm | bash -s install";
    public static final String REPO_DIR = "/opt/syncloud/repo";
    public static final String SPM_BIN = REPO_DIR + "/system/spm";

    public enum Command {Install, Verify, Upgrade, Remove}

    public static Result<String> run(Command command, Device device, String app) {
        return Ssh.execute(device, SPM_BIN + " " + command.name().toLowerCase() + " " + app);
    }

    private static Result<String> installSpm(Device device) {
        return  Ssh.execute(device, INSTALL_SPM);
    }

    public static Result<String> updateSpm(Device device) {
        return installSpm(device);
    }

    public static Result<Boolean> ensureAdminToolsInstalled(Device device, Function<String, String> progress) {
        progress.apply("getting list of apps");
        Result<List<AppVersions>> list = list(device);
        if (list.hasError())
            return Result.error(list.getError());

        for (AppVersions appVersions : filter(list.getValue(), App.Type.admin)) {

            Command command;
            if(!appVersions.installed()) {
                command = Command.Install;
            } else {
                if (!appVersions.current_version.equals(appVersions.installed_version))
                    command = Command.Upgrade;
                else
                    continue;
            }

            progress.apply("installing " + appVersions.app.name);
            Result<String> install = run(command, device, appVersions.app.id);
            if (install.hasError())
                return Result.error(install.getError());
        }

        return Result.value(true);
    }

    public static Result<List<AppVersions>> list(Device device) {

        return Ssh.execute(device, SPM_BIN + " list")
                .flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
                    @Override
                    public Result<List<AppVersions>> apply(String input) throws Exception {
                        List<AppVersions> apps = JSON.readValue(input, AppListReply.class).data;
                        apps = filterNot(apps, App.Type.system);
                        return Result.value(apps);
                    }
                });

    }

    private static List<AppVersions> filterNot(List<AppVersions> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<AppVersions>() {
            @Override
            public boolean apply(AppVersions input) {
                return input.app.appType() != type;
            }
        }));
    }

    private static List<AppVersions> filter(List<AppVersions> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<AppVersions>() {
            @Override
            public boolean apply(AppVersions input) {
                return input.app.appType() == type;
            }
        }));
    }
}
