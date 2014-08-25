package org.syncloud.ssh;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EndpointChecker {
    public static Result<DirectEndpoint> findDirectEndpoint(Device device, String type) {

        DirectEndpoint localEndpoint = device.getLocalEndpoint();
        if (visible(localEndpoint))
            return Result.value(localEndpoint);

        return dnsService(device.getUserDomain(), type, localEndpoint.getKey());
    }

    public static Result<DirectEndpoint> dnsService(String domain, String type, String key) {

        String name = type + "." + domain;
        Result<DirectEndpoint> notFound = Result.error("Public address is not available yet for " + name);

        try {
            Record[] records = new Lookup(name, Type.SRV).run();
            if (records == null)
                return notFound;
            for (Record record : records) {
                SRVRecord srvRecord = (SRVRecord) record;
                String target = srvRecord.getTarget().toString();
                String host = target.substring(0, target.length() - 1);
                DirectEndpoint endpoint = new DirectEndpoint(
                        host,
                        srvRecord.getPort(),
                        null, null, key);
                if (visible(endpoint))
                    return Result.value(endpoint);
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        return notFound;
    }

    private static boolean visible(DirectEndpoint endpoint) {
        try {
            Socket socket = new Socket();
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()), 3000);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
