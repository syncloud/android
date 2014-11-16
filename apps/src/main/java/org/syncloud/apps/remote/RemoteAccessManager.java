package org.syncloud.apps.remote;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.common.model.Result;
import org.syncloud.common.progress.Progress;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
import org.syncloud.ssh.model.StringResult;

public class RemoteAccessManager {
    public static final ObjectMapper JSON = new ObjectMapper();

    public static final int REMOTE_ACCESS_PORT = 1022;
    private static final String REMOTE_BIN = "remote";
    private InsiderManager insider;
    private Ssh ssh;

    public RemoteAccessManager(InsiderManager insider, Ssh ssh) {
        this.insider = insider;
        this.ssh = ssh;
    }

    public Result<String> disable(Device device) {
        return ssh.execute(device, REMOTE_BIN + " disable");
    }

//    This is enable method implemented in usual way it is only 4 lines
//    This example is 5 lines instead of 15 lines (9 lines in IDEA) with Result
//    This example clearly shows that using Result is just a masochism without proper language syntax support
//    Once again: even altogether with such a long comment procedural code is shorter then Result-based
//
//    public static Device enableProcedural(final Device device, final String domain) {
//        final DirectEndpoint directEndpoint = device.getLocalEndpoint();
//        String data = staticExecute(device, REMOTE_BIN + " enable");
//        final String key = JSON.readValue(data, RemoteReply.class).data;
//        String userDomain = userDomain(device);
//        return convert(userDomain + "." + domain, directEndpoint, key);
//    }

    public Result<Device> enable(final Device device, final String domain, final Progress progress) {
        final DirectEndpoint directEndpoint = device.getLocalEndpoint();
        return ssh.execute(device, REMOTE_BIN + " enable", progress)
                .flatMap(new Result.Function<String, Result<Device>>() {
                    @Override
                    public Result<Device> apply(final String data) throws Exception {
                        final String key = JSON.readValue(data, StringResult.class).data;
                        return insider.userDomain(device, progress)
                                .map(new Result.Function<String, Device>() {
                                    @Override
                                    public Device apply(String userDomain) throws Exception {
                                        return convert(userDomain + "." + domain, directEndpoint, key);
                                    }
                                });
                    }
                });
    }

    private static Device convert(String userDomain, DirectEndpoint directEndpoint, String key) {
        return new Device(
                null,
                null,
                userDomain,
                new DirectEndpoint(directEndpoint.getHost(), REMOTE_ACCESS_PORT, "root", "syncloud", key)
        );
    }
}