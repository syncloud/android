package org.syncloud.apps.sam;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

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

    public Result<String> run(Device device, Command command, String... arguments) {
        Result<String> installResult = ensureSamInstalled(device);
        if (installResult.hasError())
            return installResult;

        return ssh.execute(device, command.cmd(arguments));
    }

    private Result<String> ensureSamInstalled(Device device) {
        Result<String> exists = ssh.execute(device, SAM_EXIST_COMMAND);
        if (exists.hasError())
            return ssh.execute(device, SAM_BOOTSTRAP_COMMAND);
        return exists;
    }

    public Result<String> update(Device device) {
        return run(device, Update, "--release", RELEASE);
    }

    public Result<List<AppVersions>> list(Device device) {

        return run(device, List)
                .flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
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
