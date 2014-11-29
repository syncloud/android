package org.syncloud.ssh;

import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;

public class Dns {
    public Record[] lookup(String name, int type) throws Exception {
        Lookup lookup = new Lookup(name, type);
        lookup.setResolver(new ExtendedResolver(new String[] {"8.8.8.8", "8.8.4.4"} ) );
        return lookup.run();
    }
}
