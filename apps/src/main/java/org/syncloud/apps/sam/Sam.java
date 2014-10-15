package org.syncloud.apps.sam;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.syncloud.common.progress.Progress;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.syncloud.apps.sam.App.Type.system;
import static org.syncloud.apps.sam.Command.List;
import static org.syncloud.apps.sam.Command.Update;
import static org.syncloud.common.model.Result.value;

public class Sam {
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final String RELEASE = "0.7";
    private static final String SAM_BOOTSTRAP_COMMAND =
            "wget -qO- https://raw.githubusercontent.com/syncloud/apps/"+ RELEASE + "/sam | bash -s bootstrap";
    public static final String SAM_EXIST_COMMAND = "type sam";
    private Ssh ssh;

    public Sam(Ssh ssh) {
        this.ssh = ssh;
    }

    public Result<String> run(Device device, Progress progress, Command command, String... arguments) {

        Result<String> installResult = ensureSamInstalled(device, progress);
        if (installResult.hasError())
            return installResult;

        String cmd = command.cmd(arguments);
        progress.title("Running " + cmd);
        return ssh.execute(device, cmd);
    }

    private Result<String> ensureSamInstalled(Device device, Progress progress) {
        progress.title("Checking app manager");
        Result<String> exists = ssh.execute(device, SAM_EXIST_COMMAND);
        if (exists.hasError())
            return ssh.execute(device, SAM_BOOTSTRAP_COMMAND);
        return exists;
    }

    public Boolean update(Device device, Progress progress) {

        progress.title("Checking for updates");

        Result<String> result = run(device, progress, Update, "--release", RELEASE);
        if (result.hasError()) {
            progress.error(result.getError());
            return false;
        }
        return true;
    }

    public Result<List<AppVersions>> list(Device device, Progress progress) {

        progress.title("Refreshing app list");

        Result<String> run = run(device, progress, List);
        if (run.hasError())
            progress.error(run.getError());

        return run.flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
            @Override
            public Result<List<AppVersions>> apply(String v) throws Exception {
                return value(filterNot(JSON.readValue(v, AppListReply.class).data, system));
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

}
