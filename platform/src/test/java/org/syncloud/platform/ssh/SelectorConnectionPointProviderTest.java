package org.syncloud.platform.ssh;

import org.junit.Ignore;
import org.junit.Test;
import org.syncloud.platform.ssh.model.ConnectionPoint;
import org.syncloud.platform.ssh.model.Device;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelectorConnectionPointProviderTest {

    @Test()
    @Ignore
    public void testPreferredEndpointIsDown() {

        EndpointSelector selector = mock(EndpointSelector.class);
//        SshRunner sshRunner = mock(SshRunner.class);

        EndpointPreference preference = mock(EndpointPreference.class);
        SelectorConnectionPointProvider provider = new SelectorConnectionPointProvider(
                selector,
                preference,
                mock(Device.class));

        ConnectionPoint first = mock(ConnectionPoint.class);
        when(selector.select(any(Device.class), eq(true))).thenReturn(first);

        ConnectionPoint second = mock(ConnectionPoint.class);
        when(selector.select(any(Device.class), eq(false))).thenReturn(second);

//        when(sshRunner.run(any(ConnectionPoint.class), any(String[].class)))
//                .thenThrow(new RuntimeException("not available"))
//                .thenReturn("available");

        ConnectionPoint connectionPoint = provider.get();

        assertEquals(second, connectionPoint);
        verify(preference).swap();
    }

    @Test
    public void testPreferredEndpointIsUp() {

        EndpointSelector selector = mock(EndpointSelector.class);
//        SshRunner sshRunner = mock(SshRunner.class);

        EndpointPreference preference = mock(EndpointPreference.class);
        SelectorConnectionPointProvider provider = new SelectorConnectionPointProvider(
                selector,
                preference,
                mock(Device.class));

        ConnectionPoint first = mock(ConnectionPoint.class);
        when(selector.select(any(Device.class), eq(true))).thenReturn(first);

//        when(sshRunner.run(any(ConnectionPoint.class), any(String[].class)))
//                .thenReturn("available");

        ConnectionPoint connectionPoint = provider.get();

        assertEquals(first, connectionPoint);
        verify(preference, times(0)).swap();
    }
}
