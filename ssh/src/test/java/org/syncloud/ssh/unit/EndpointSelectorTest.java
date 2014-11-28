package org.syncloud.ssh.unit;

import junit.framework.Assert;

import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.EndpointPreference;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.EndpointSelector;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndpointSelectorTest {

    private final int remotePort = 10000;
    private final String domain = "syncloud.it";
    private final String remoteHost = "device." + domain;
    private Endpoint remoteEndpoint = new Endpoint(remoteHost, remotePort);

    private Endpoint localEndpoint = new Endpoint("localhost", 0);
    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device("0", null, userDomain, localEndpoint, new Credentials("login", "password", "key"));

    @Test
    public void testPreferred() {

        EndpointResolver resolver = mock(EndpointResolver.class);
        when(resolver.dnsService(anyString(), anyString())).thenReturn(Result.value(remoteEndpoint));

        EndpointPreference preference = mock(EndpointPreference.class);

        EndpointSelector selector = new EndpointSelector(resolver, preference);

        when(preference.isRemote()).thenReturn(false);
        assertEquals(localEndpoint, selector.first(device).getValue());
        assertEquals(remoteEndpoint, selector.second(device).getValue());

        when(preference.isRemote()).thenReturn(true);
        assertEquals(remoteEndpoint, selector.first(device).getValue());
        assertEquals(localEndpoint, selector.second(device).getValue());
    }

}
