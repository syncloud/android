package org.syncloud.ssh.unit;

import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.EndpointVisibility;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
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

    private String type = "_ssh._tcp";
    private final String userDomain = "syncloud.it";
    private final String remoteHost = "device." + userDomain;

    private DirectEndpoint localEndpoint = new DirectEndpoint("localEndpoint", 0, "", "", "");
    private DirectEndpoint remoteEndpoint = new DirectEndpoint(remoteHost, remotePort, "", "", "");

    private final Device device = new Device(0, "", "testdomain1.syncloud.info", localEndpoint);


    @Test
    public void test_local_visible() {

        EndpointVisibility visibility = mock(EndpointVisibility.class);
        when(visibility.visible(localEndpoint)).thenReturn(true);

        EndpointResolver resolver = new EndpointResolver(mock(Dns.class), visibility);

        Result<DirectEndpoint> endpoint = resolver.findDirectEndpoint(device, "_ssh._tcp");

        assertFalse(endpoint.hasError());
        assertEquals(localEndpoint, endpoint.getValue());
    }

    @Test
    public void test_remote_not_found() {

        Dns dns = mock(Dns.class);
        EndpointVisibility visibility = mock(EndpointVisibility.class);
        when(visibility.visible(localEndpoint)).thenReturn(false);

        EndpointResolver resolver = new EndpointResolver(dns, visibility);

        Result<DirectEndpoint> endpoint = resolver.findDirectEndpoint(device, "_ssh._tcp");

        assertTrue(endpoint.hasError());
    }

    @Test
    public void test_remote_one_found() throws TextParseException {

        Dns dns = mock(Dns.class);
        SRVRecord record = new SRVRecord(fromString(type + "." + userDomain + "."), 1, 1,1, 1, remotePort, fromString(remoteHost + "."));
        when(dns.lookup(anyString(), anyInt())).thenReturn(new Record[] {record});
        EndpointVisibility visibility = mock(EndpointVisibility.class);
        when(visibility.visible(localEndpoint)).thenReturn(false);
        when(visibility.visible(remoteEndpoint)).thenReturn(true);

        EndpointResolver resolver = new EndpointResolver(dns, visibility);

        Result<DirectEndpoint> endpoint = resolver.findDirectEndpoint(device, type);

        assertFalse(endpoint.hasError() ? endpoint.getError() : "", endpoint.hasError());
        assertEquals(remoteEndpoint, endpoint.getValue());
    }

    @Test
    public void test_remote_not_visible() throws TextParseException {

        Dns dns = mock(Dns.class);
        SRVRecord record = new SRVRecord(fromString(type + "." + userDomain + "."), 1, 1,1, 1, remotePort, fromString(remoteHost + "."));
        when(dns.lookup(anyString(), anyInt())).thenReturn(new Record[] {record});
        EndpointVisibility visibility = mock(EndpointVisibility.class);
        when(visibility.visible(localEndpoint)).thenReturn(false);
        when(visibility.visible(remoteEndpoint)).thenReturn(false);

        EndpointResolver resolver = new EndpointResolver(dns, visibility);

        Result<DirectEndpoint> endpoint = resolver.findDirectEndpoint(device, type);

        assertTrue(endpoint.hasError());
    }

    @Test
    public void test_dns_exception() throws TextParseException {

        Dns dns = mock(Dns.class);
        when(dns.lookup(anyString(), anyInt())).thenThrow(new TextParseException());
        EndpointVisibility visibility = mock(EndpointVisibility.class);
        when(visibility.visible(localEndpoint)).thenReturn(false);

        EndpointResolver resolver = new EndpointResolver(dns, visibility);

        Result<DirectEndpoint> endpoint = resolver.findDirectEndpoint(device, type);

        assertTrue(endpoint.hasError());
    }
}
