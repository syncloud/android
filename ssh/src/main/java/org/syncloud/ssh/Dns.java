package org.syncloud.ssh;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;

public class Dns {
    public Record[] lookup(String name, int type) throws TextParseException {
        return new Lookup(name, type).run();
    }
}
