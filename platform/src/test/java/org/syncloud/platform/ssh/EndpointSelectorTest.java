package org.syncloud.platform.ssh;

import org.junit.Test;
import org.syncloud.platform.ssh.EndpointPreference;
import org.syncloud.platform.ssh.EndpointSelector;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.Device;
import org.syncloud.platform.ssh.model.Endpoint;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndpointSelectorTest {

    private final int remotePort = 10000;
    private final String user_domain = "syncloud.it";
    private final String remoteHost = "domain." + user_domain;
    private Endpoint remoteEndpoint = new Endpoint(remoteHost, remotePort);
    private Endpoint localEndpoint = new Endpoint("localhost", 0);
    private Credentials credentials = new Credentials("login", "password", "key");

    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device(null, localEndpoint, remoteEndpoint, credentials);

    @Test
    public void testPreferred() {
        EndpointPreference preference = mock(EndpointPreference.class);

        EndpointSelector selector = new EndpointSelector(preference);

        when(preference.isRemote()).thenReturn(false);
        assertEquals(localEndpoint, selector.select(device, true).get().endpoint());
        assertEquals(remoteEndpoint, selector.select(device, false).get().endpoint());

        when(preference.isRemote()).thenReturn(true);
        assertEquals(remoteEndpoint, selector.select(device, true).get().endpoint());
        assertEquals(localEndpoint, selector.select(device,false).get().endpoint());
    }

}
