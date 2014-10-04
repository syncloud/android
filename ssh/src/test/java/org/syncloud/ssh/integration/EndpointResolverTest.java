package org.syncloud.ssh.integration;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;

public class EndpointResolverTest {

    @Test
    public void testDnsSrv() {

        EndpointResolver resolver = new EndpointResolver(new Dns());
        Device device = new Device(0, "",
                "testdomain1.syncloud.info",
                new DirectEndpoint("localhost", 0, "", "", ""));
        Result<DirectEndpoint> endpoint = resolver.dnsService(device.getDisplayName(), "_ssh._tcp", device.getLocalEndpoint().getKey());

        Assert.assertFalse(endpoint.hasError());

    }
}
