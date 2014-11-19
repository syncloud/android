package org.syncloud.ssh.unit;

import com.jcraft.jsch.JSchException;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.common.progress.Progress;
import org.syncloud.ssh.EndpointPreference;
import org.syncloud.ssh.EndpointSelector;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SshTest {

    private Endpoint localEndpoint = new Endpoint("localhost", 0);
    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device(0, userDomain, localEndpoint, new Credentials("login", "password", "key"));

    @Test
    public void testExecute_UnableToConnect() throws JSchException, IOException {

        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(Endpoint.class), any(Credentials.class), anyString()))
                .thenThrow(new JSchException("bad"))
                .thenThrow(new JSchException("bad"));

        EndpointSelector resolver = mock(EndpointSelector.class);
        when(resolver.first(any(Device.class))).thenReturn(Result.value(localEndpoint));
        when(resolver.second(any(Device.class))).thenReturn(Result.value(localEndpoint));

        Progress progress = mock(Progress.class);
        EndpointPreference preference = mock(EndpointPreference.class);
        Ssh ssh = new Ssh(runner, resolver, progress, preference);

        Result<String> result = ssh.execute(device, "command");

        Assert.assertTrue(result.hasError());
        verify(progress, atLeastOnce()).error(anyString());
        verify(preference, times(0)).swap();

    }

    @Test
    public void testExecute_Preferred_Good() throws JSchException, IOException {

        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(Endpoint.class), any(Credentials.class), anyString()))
                .thenReturn(Result.value("good"));

        EndpointSelector selector = mock(EndpointSelector.class);
        when(selector.first(any(Device.class))).thenReturn(Result.value(localEndpoint));
        when(selector.second(any(Device.class))).thenReturn(Result.value(localEndpoint));

        Progress progress = mock(Progress.class);
        EndpointPreference preference = mock(EndpointPreference.class);
        when(preference.isRemote()).thenReturn(true);
        Ssh ssh = new Ssh(runner, selector, progress, preference);

        Result<String> result = ssh.execute(device, "command");

        Assert.assertFalse(result.hasError());
        verify(progress, times(0)).error(anyString());
        verify(selector).first(any(Device.class));
        verify(preference, times(0)).swap();

    }

    @Test
    public void testExecute_Preferred_Bad() throws JSchException, IOException {

        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(Endpoint.class), any(Credentials.class), anyString()))
                .thenThrow(new JSchException("bad"))
                .thenReturn(Result.value("good"));

        EndpointSelector selector = mock(EndpointSelector.class);
        when(selector.first(any(Device.class))).thenReturn(Result.value(localEndpoint));
        when(selector.second(any(Device.class))).thenReturn(Result.value(localEndpoint));

        Progress progress = mock(Progress.class);
        EndpointPreference preference = mock(EndpointPreference.class);
        when(preference.isRemote()).thenReturn(true);
        Ssh ssh = new Ssh(runner, selector, progress, preference);

        Result<String> result = ssh.execute(device, "command");

        Assert.assertFalse(result.hasError());
        verify(progress, times(0)).error(anyString());
        verify(selector).first(any(Device.class));
        verify(selector).second(any(Device.class));
        verify(preference).swap();

    }

}
