package org.syncloud.insider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.DeviceReply;
import org.syncloud.insider.model.Endpoint;
import org.syncloud.insider.model.EndpointResult;
import org.syncloud.ssh.model.Device;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.syncloud.ssh.Ssh.execute1;

public class InsiderManager {

    private static final String INSIDER_BIN = "insider";
    public static final ObjectMapper JSON = new ObjectMapper();

    public static Result<String> addService(
            final Device device, String name, String protocol, String type, int port, String url) {

        return execute1(
                device,
                asList(format(
                        "%s add_service %s %s %s %s %s",
                        INSIDER_BIN, name, protocol, type, port, url)))
                .flatMap(new Result.Function<String, Result<String>>() {
            @Override
            public Result<String> apply(String input) throws Exception {
                return enableCron(device);
            }
        });

    }

    public static Result<Endpoint> serviceInfo(Device device, String name) {
        return execute1(device, asList(format("%s service_info %s", INSIDER_BIN, name)))
                .map(new Result.Function<String, Endpoint>() {
                    @Override
                    public Endpoint apply(String input) throws Exception {
                        return JSON.readValue(input, EndpointResult.class).getData();
                    }
                });
    }

    private static Result<String> enableCron(Device device) {
        return execute1(device, asList(INSIDER_BIN + " cron_on "));
    }

    public static Result<String> acquireDomain(
            final Device device, String email, String pass, String domain) {

        return execute1(
                device,
                asList(format("%s acquire_domain %s %s %s", INSIDER_BIN, email, pass, domain)))
                .flatMap(new Result.Function<String, Result<String>>() {
                    @Override
                    public Result<String> apply(String input) throws Exception {
                        return enableCron(device);
                    }
                });
    }

    public static Result<String> setRedirectInfo(Device device, String domain, String apiUl) {

        return execute1(
                device,
                asList(format("%s set_redirect_info %s %s", INSIDER_BIN, domain, apiUl)));

    }

    public static Result<String> dropDomain(Device device) {
        return execute1(device, asList(format("%s drop_domain", INSIDER_BIN)));
    }

    public static Result<String> fullName(Device device) {
        return execute1(device, asList(INSIDER_BIN + " full_name"))
                .map(new Result.Function<String, String>() {
                    @Override
                    public String apply(String input) throws Exception {
                        return JSON.readValue(input, DeviceReply.class).getData();
                    }
                });

    }

    public static Result<String> removeService(Device device, int port) {
        return execute1(device, asList(INSIDER_BIN + " remove_service " + port));
    }
}
