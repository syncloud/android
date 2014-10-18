package org.syncloud.apps.insider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.StringResult;

import static java.lang.String.format;

public class InsiderManager {

    private static final String INSIDER_BIN = "insider";
    public static final ObjectMapper JSON = new ObjectMapper();

    public static Result<String> userDomain(Device device) {
        return Ssh.staticExecute(device, format("%s user_domain", INSIDER_BIN))
                .map(new Result.Function<String, String>() {
                    @Override
                    public String apply(String input) throws Exception {
                        return JSON.readValue(input, StringResult.class).data;
                    }
                });
    }

    public static Result<String> acquireDomain(final Device device, String email, String pass, String domain) {
        return Ssh.staticExecute(device, format("%s acquire_domain %s %s %s", INSIDER_BIN, email, pass, domain));
    }

    public static Result<String> setRedirectInfo(Device device, String domain, String apiUl) {
        return Ssh.staticExecute(device, format("%s set_redirect_info %s %s", INSIDER_BIN, domain, apiUl));
    }

    public static Result<String> dropDomain(Device device) {
        return Ssh.staticExecute(device, format("%s drop_domain", INSIDER_BIN));
    }
}
