package org.syncloud.apps.owncloud;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.StringResult;

import static java.util.Arrays.asList;
import static org.syncloud.ssh.Ssh.execute;

public class OwncloudManager {

    private static String OWNCLOUD_CTL_BIN = "owncloud-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();

    public static Result<String> finishSetup(Device device, String login, String password) {
        return execute(device, asList(String.format("%s finish %s %s", OWNCLOUD_CTL_BIN, login, password)));
    }

    public static Result<String> owncloudUrl(Device device) {

        Result<String> execute = execute(device, asList(String.format("%s url", OWNCLOUD_CTL_BIN)));
        return execute.map(new Result.Function<String, String>() {
            @Override
            public String  apply(String input) throws Exception {
                return JSON.readValue(input, StringResult.class).data;
            }
        });

    }

}
