package org.syncloud.ssh.unit;

import org.junit.Test;
import org.syncloud.ssh.EndpointPreference;
import org.syncloud.ssh.EndpointSelector;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndpointSelectorTest {

    private final int remotePort = 10000;
    private final String domain = "syncloud.it";
    private final String remoteHost = "device." + domain;
    private Endpoint remoteEndpoint = new Endpoint(remoteHost, remotePort);
    private Endpoint localEndpoint = new Endpoint("localhost", 0);
    private Credentials credentials = new Credentials("login", "password", "key");

    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device(null, userDomain, localEndpoint, remoteEndpoint, credentials);

    @Test
    public void testPreferred() {
        EndpointPreference preference = mock(EndpointPreference.class);

        EndpointSelector selector = new EndpointSelector(preference);

        when(preference.isRemote()).thenReturn(false);
        assertEquals(localEndpoint, selector.select(device, true).get().endpoint());
        assertEquals(remoteEndpoint, selector.select(device, false).get().endpoint());

        when(preference.isRemote()).thenReturn(true);
        assertEquals(remoteEndpoint, selector.select(device, true).get().endpoint());
        assertEquals(localEndpoint, selector.select(device ,false).get().endpoint());
    }

}
