package org.syncloud.ssh;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class EndpointResolver {

    private Dns dns;
    private EndpointVisibility visibility;

    public EndpointResolver(Dns dns, EndpointVisibility visibility) {
        this.dns = dns;
        this.visibility = visibility;
    }

    public Result<DirectEndpoint> findDirectEndpoint(Device device, String type) {

        DirectEndpoint localEndpoint = device.getLocalEndpoint();
        if (visibility.visible(localEndpoint))
            return Result.value(localEndpoint);

        return dnsService(device.getUserDomain(), type, localEndpoint.getKey());
    }

    private Result<DirectEndpoint> dnsService(String domain, String type, String key) {

        String name = type + "." + domain;
        Result<DirectEndpoint> notFound = Result.error("Public address is not available yet for " + name);

        try {
            Record[] records = dns.lookup(name, Type.SRV);
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
                if (visibility.visible(endpoint))
                    return Result.value(endpoint);
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        return notFound;
    }




}
