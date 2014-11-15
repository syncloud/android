package org.syncloud.ssh.unit;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.common.progress.Progress;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SshTest {

    private final int remotePort = 10000;
    private final String domain = "syncloud.it";
    private final String remoteHost = "device." + domain;

    private DirectEndpoint localEndpoint = new DirectEndpoint("localEndpoint", 0, "", "", "");
    private DirectEndpoint remoteEndpoint = new DirectEndpoint(remoteHost, remotePort, "", "", "");

    private final String userDomain = "testdomain1.syncloud.info";
    private final Device device = new Device(0, "", userDomain, localEndpoint);

    @Test
    public void testExecute_UnableToConnect() throws JSchException {

        JSch jsch = mock(JSch.class);
        when(jsch.getSession(anyString(), anyString(), anyInt())).thenThrow(new JSchException("unable to connect"));

        EndpointResolver resolver = mock(EndpointResolver.class);
        when(resolver.dnsService(anyString(), anyString(), anyString())).thenReturn(Result.value(remoteEndpoint));

        Ssh ssh = new Ssh(jsch, resolver);
        Progress progress = mock(Progress.class);

        Result<String> result = ssh.execute(device, "command", progress);

        Assert.assertTrue(result.hasError());
        verify(progress, atLeastOnce()).error(anyString());

    }

    @Test
    public void testExecute_UnableToResolveDns() throws JSchException {

        JSch jsch = mock(JSch.class);
        when(jsch.getSession(anyString(), anyString(), anyInt())).thenThrow(new JSchException("unable to connect"));

        EndpointResolver resolver = mock(EndpointResolver.class);
        when(resolver.dnsService(anyString(), anyString(), anyString())).thenReturn(Result.<DirectEndpoint>error("not available"));

        Ssh ssh = new Ssh(jsch, resolver);
        Progress progress = mock(Progress.class);

        Result<String> result = ssh.execute(device, "command", progress);

        Assert.assertTrue(result.hasError());
        verify(progress, atLeastOnce()).error(anyString());

    }
}
