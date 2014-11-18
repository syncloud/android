package org.syncloud.ssh;

import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Endpoint;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class EndpointResolver {

    private Dns dns;

    public EndpointResolver(Dns dns) {
        this.dns = dns;
    }

    public Result<Endpoint> dnsService(String domain, String type) {

        String name = type + "." + domain;
        Result<Endpoint> notFound = Result.error("Public address is not available yet for " + name);

        try {
            Record[] records = dns.lookup(name, Type.SRV);
            if (records == null)
                return notFound;
            if (records.length > 0) {
                SRVRecord srvRecord = (SRVRecord) records[0];
                String target = srvRecord.getTarget().toString();
                String host = target.substring(0, target.length() - 1);
                Endpoint endpoint = new Endpoint(host, srvRecord.getPort());
                return Result.value(endpoint);
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        return notFound;
    }

}
