package org.syncloud.ssh.unit;

import com.google.common.base.Optional;
import com.jcraft.jsch.JSchException;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.ssh.EndpointPreference;
import org.syncloud.ssh.EndpointSelector;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SshTest {

    private Endpoint localEndpoint = new Endpoint("localhost", 0);
    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device("0", null, userDomain, localEndpoint, new Credentials("login", "password", "key"));

    @Test
    public void testExecute_UnableToConnect() throws JSchException, IOException {

        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(Endpoint.class), any(Credentials.class), anyString()))
                .thenReturn(Optional.<String>absent())
                .thenReturn(Optional.<String>absent());

        EndpointSelector resolver = mock(EndpointSelector.class);
        when(resolver.select(any(Device.class), eq(true))).thenReturn(Optional.of(localEndpoint));
        when(resolver.select(any(Device.class), eq(false))).thenReturn(Optional.of(localEndpoint));

        EndpointPreference preference = mock(EndpointPreference.class);
        Ssh ssh = new Ssh(runner, resolver, preference);

        Optional<String> result = ssh.execute(device, "command");

        Assert.assertFalse(result.isPresent());
        verify(preference, times(0)).swap();

    }

    @Test
    public void testExecute_Preferred_Good() {

        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(Endpoint.class), any(Credentials.class), anyString()))
                .thenReturn(Optional.of("good"));

        EndpointSelector selector = mock(EndpointSelector.class);
        when(selector.select(any(Device.class), eq(true))).thenReturn(Optional.of(localEndpoint));
        when(selector.select(any(Device.class), eq(false))).thenReturn(Optional.of(localEndpoint));

        EndpointPreference preference = mock(EndpointPreference.class);
        when(preference.isRemote()).thenReturn(true);
        Ssh ssh = new Ssh(runner, selector, preference);

        Optional<String> result = ssh.execute(device, "command");

        Assert.assertTrue(result.isPresent());
        verify(selector).select(any(Device.class), eq(true));
        verify(preference, times(0)).swap();

    }

    @Test
    public void testExecute_Preferred_Bad() throws JSchException, IOException {

        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(Endpoint.class), any(Credentials.class), anyString()))
                .thenReturn(Optional.<String>absent())
                .thenReturn(Optional.of("good"));

        EndpointSelector selector = mock(EndpointSelector.class);
        when(selector.select(any(Device.class), eq(true))).thenReturn(Optional.of(localEndpoint));
        when(selector.select(any(Device.class), eq(false))).thenReturn(Optional.of(localEndpoint));

        EndpointPreference preference = mock(EndpointPreference.class);
        when(preference.isRemote()).thenReturn(true);
        Ssh ssh = new Ssh(runner, selector, preference);

        Optional<String> result = ssh.execute(device, "command");

        Assert.assertTrue(result.isPresent());
        verify(selector).select(any(Device.class), eq(true));
        verify(selector).select(any(Device.class), eq(false));
        verify(preference).swap();

    }

}
