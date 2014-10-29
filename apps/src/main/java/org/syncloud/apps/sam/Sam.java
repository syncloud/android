package org.syncloud.apps.sam;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.progress.NoErrorsProgress;
import org.syncloud.common.progress.Progress;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.syncloud.apps.sam.Command.List;
import static org.syncloud.apps.sam.Command.Update;
import static org.syncloud.common.model.Result.value;

public class Sam {
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final String RELEASE = "0.7";
    private Ssh ssh;

    public Sam(Ssh ssh) {
        this.ssh = ssh;
    }

    public Result<String> run(Device device, Progress progress, Command command, String... arguments) {

        return ssh.execute(device, command.cmd(arguments), progress);
    }

    public Result<List<AppVersions>> update(Device device, Progress progress) {
        progress.title("Checking for updates");
        return appList(device, progress, Update, "--release", RELEASE);
    }

    public Result<List<AppVersions>> list(Device device, Progress progress) {
        progress.title("Refreshing app list");
        return appList(device, progress, List);
    }

    private Result<List<AppVersions>> appList(Device device, Progress progress, Command command, String... arguments) {
        return run(device, progress, command, arguments).flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
            @Override
            public Result<List<AppVersions>> apply(String v) throws Exception {
                return value(JSON.readValue(v, AppListReply.class).data);
            }
        });
    }

}
