package org.syncloud.apps.sam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.progress.Progress;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.syncloud.apps.sam.Command.Update;
import static org.syncloud.common.model.Result.value;

public class Sam {
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final String RELEASE = "0.7";
    private Ssh ssh;

    public Sam(Ssh ssh) {
        this.ssh = ssh;
    }

    private String cmd(String... arguments) {
        List<String> cmd = asList(arguments);
        cmd.add(0, "sam");

        return join(cmd, " ");
    }

    public <TContent> Result<TContent> runTyped(Device device, String... arguments) {
        return run(device, arguments).flatMap(new Result.Function<String, Result<TContent>>() {
            @Override
            public Result<TContent> apply(String v) throws Exception {
                TContent content = JSON.readValue(v, new TypeReference<TContent>() {});
                return value(content);
            }
        });
    }

    public Result<String> run(Device device, String... arguments) {
        String command = cmd(arguments);
        return ssh.execute(device, command);
    }

    public Result<String> run(Device device, Command command, String... arguments) {
        return ssh.execute(device, command.cmd(arguments));
    }

    public Result<List<AppVersions>> update(Device device) {
        progress.title("Checking for updates");
        return appList(device, Update, "--release", RELEASE);
    }

    public Result<List<AppVersions>> list(Device device) {
        return appList(device, List);
        progress.title("Refreshing app list");
        return runTyped(device, Commands.list);
    }

    private Result<List<AppVersions>> appList(Device device, Command command, String... arguments) {
        return run(device, command, arguments).flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
            @Override
            public Result<List<AppVersions>> apply(String v) throws Exception {
                return value(JSON.readValue(v, AppListReply.class).data);
            }
        });
    }

}
