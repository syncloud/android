package org.syncloud.apps.owncloud;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.StringResult;

public class OwncloudManager {

    private static String OWNCLOUD_CTL_BIN = "owncloud-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();
    private Ssh ssh;

    public OwncloudManager(Ssh ssh) {
        this.ssh = ssh;
    }

    public Result<String> finishSetup(Device device, String login, String password, String protocol) {
        return ssh.execute(device, String.format("%s finish %s %s %s", OWNCLOUD_CTL_BIN, login, password, protocol));
    }

    public Result<String> owncloudUrl(Device device) {

        Result<String> execute = ssh.execute(device, String.format("%s url", OWNCLOUD_CTL_BIN));
        return execute.map(new Result.Function<String, String>() {
            @Override
            public String  apply(String input) throws Exception {
                return JSON.readValue(input, StringResult.class).data;
            }
        });

    }

}
