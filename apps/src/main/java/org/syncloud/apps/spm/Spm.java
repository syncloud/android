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

    public enum Command {Install, Verify, Upgrade, Remove, Status}

    public static Result<String> run(Command command, Device device, String app) {
        return Ssh.execute(device, SPM_BIN + " " + command.name().toLowerCase() + " " + app);
    }

    private static Result<String> installSpm(Device device) {
        return  Ssh.execute(device, INSTALL_SPM);
    }

    private static Result<String> spmInstalled(Device device) {
        return Ssh.execute(device, "[ -d " + REPO_DIR + " ]");
    }

    public static Result<String> updateSpm(Device device) {
        return installSpm(device);
    }

    public static Result<String> ensureSpmInstalled(final Device device) {
        Result<String> installed = spmInstalled(device);
        if (installed.hasError()) {
            return installSpm(device);
        }
        return installed;
    }

    public static Result<Boolean> ensureAdminToolsInstalled(Device device, Function<String, String> progress) {

        progress.apply("installing spm");
        Result<String> result = ensureSpmInstalled(device);
        if (result.hasError())
            return Result.error(result.getError());

        progress.apply("getting list of apps");
        Result<List<App>> list = list(device);
        if (list.hasError())
            return Result.error(list.getError());

        for (App app : filter(list.getValue(), App.Type.admin)) {

            Command command;
            if(!app.installed()) {
                command = Command.Install;
            } else {
                if (!app.version.equals(app.installed_version))
                    command = Command.Upgrade;
                else
                    continue;
            }

            progress.apply("installing " + app.name);
            Result<String> install = run(command, device, app.id);
            if (install.hasError())
                return Result.error(install.getError());
        }

        return Result.value(true);
    }

    public static Result<List<App>> list(Device device) {

        return Ssh.execute(device, SPM_BIN + " list")
                .flatMap(new Result.Function<String, Result<List<App>>>() {
                    @Override
                    public Result<List<App>> apply(String input) throws Exception {
                        List<App> apps = JSON.readValue(input, AppListReply.class).data;
                        apps = filterNot(apps, App.Type.system);
                        return Result.value(apps);
                    }
                });

    }

    private static List<App> filterNot(List<App> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<App>() {
            @Override
            public boolean apply(App input) {
                return input.appType() != type;
            }
        }));
    }

    private static List<App> filter(List<App> apps, final App.Type type) {
        return  Lists.newArrayList(Iterables.filter(apps, new Predicate<App>() {
            @Override
            public boolean apply(App input) {
                return input.appType() == type;
            }
        }));
    }
}
