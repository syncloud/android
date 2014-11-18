package org.syncloud.ssh.unit;

import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xbill.DNS.Name.fromString;

public class EndpointResolverTest {

    private final int remotePort = 10000;
    private final String type = "_ssh._tcp";

    private final String domain = "syncloud.it";
    private final String remoteHost = "device." + domain;

    private Endpoint localEndpoint = new Endpoint("localEndpoint", 0);
    private Endpoint remoteEndpoint = new Endpoint(remoteHost, remotePort);

    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device(0, userDomain, localEndpoint, new Credentials("", "", ""));


    @Test
    public void test_remote_not_found() {

        EndpointResolver resolver = new EndpointResolver(mock(Dns.class));

        Result<Endpoint> endpoint = resolver.dnsService(userDomain, type);

        assertTrue(endpoint.hasError());
    }

    @Test
    public void test_remote_one_found() throws TextParseException {

        Dns dns = mock(Dns.class);
        SRVRecord record = new SRVRecord(fromString(type + "." + domain + "."), 1, 1,1, 1, remotePort, fromString(remoteHost + "."));
        when(dns.lookup(anyString(), anyInt())).thenReturn(new Record[] {record});

        EndpointResolver resolver = new EndpointResolver(dns);

        Result<Endpoint> endpoint = resolver.dnsService(userDomain, type);

        assertFalse(endpoint.hasError() ? endpoint.getError() : "", endpoint.hasError());
        assertEquals(remoteEndpoint, endpoint.getValue());
    }

    @Test
    public void test_dns_exception() throws TextParseException {

        Dns dns = mock(Dns.class);
        when(dns.lookup(anyString(), anyInt())).thenThrow(new TextParseException());

        EndpointResolver resolver = new EndpointResolver(dns);

        Result<Endpoint> endpoint = resolver.dnsService(userDomain, type);

        assertTrue(endpoint.hasError());
    }
}
