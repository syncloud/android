package org.syncloud.ssh;

import org.apache.log4j.Logger;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Endpoint;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class EndpointResolver {

    private static Logger logger = Logger.getLogger(EndpointResolver.class);

    private Dns dns;

    public EndpointResolver(Dns dns) {
        this.dns = dns;
    }

    public Result<Endpoint> dnsService(String domain, String type) {

        String name = type + "." + domain;
        Result<Endpoint> notFound = Result.error("Public address is not available yet for " + name);

        try {
            logger.info("looking up dns srv record: " + name);
            Record[] records = dns.lookup(name, Type.SRV);
            if (records == null) {
                logger.error("dns srv not found");
                return notFound;
            }
            if (records.length > 0) {
                logger.info("found dns srv records: " + records.length);
                SRVRecord srvRecord = (SRVRecord) records[0];
                String target = srvRecord.getTarget().toString();
                String host = target.substring(0, target.length() - 1);
                Endpoint endpoint = new Endpoint(host, srvRecord.getPort());
                return Result.value(endpoint);
            }

            logger.warn("got empty dns srv reply");

        } catch (Exception e) {
            logger.error("unable to parse dns srv reply: " + e.getMessage());
        }

        return notFound;
    }

}
