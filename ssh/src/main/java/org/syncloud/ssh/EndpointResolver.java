package org.syncloud.ssh;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.model.Endpoint;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

public class EndpointResolver {

    private static Logger logger = Logger.getLogger(EndpointResolver.class);

    private Dns dns;

    public EndpointResolver(Dns dns) {
        this.dns = dns;
    }

    public Optional<Endpoint> dnsService(String domain, String type) {

        String name = type + "." + domain;

        try {
            logger.info("looking up dns srv record: " + name);
            Record[] records = dns.lookup(name, Type.SRV);
            if (records == null) {
                logger.error("dns srv not found");
                return Optional.absent();
            }
            if (records.length > 0) {
                logger.info("found dns srv records: " + records.length);
                SRVRecord srvRecord = (SRVRecord) records[0];
                String target = srvRecord.getTarget().toString();
                String host = target.substring(0, target.length() - 1);
                return Optional.of(new Endpoint(host, srvRecord.getPort()));
            }

            logger.warn("got empty dns srv reply");

        } catch (Exception e) {
            logger.error("unable to parse dns srv reply: " + e.getMessage());
        }

        return Optional.absent();
    }

}
