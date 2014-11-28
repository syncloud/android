package org.syncloud.ssh.integration;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

public class EndpointResolverTest {

    @Test
    public void testDnsSrv() {

        EndpointResolver resolver = new EndpointResolver(new Dns());
        Device device = new Device(
                "0",
                null,
                "testdomain1.syncloud.info",
                new Endpoint("localhost", 0),
                new Credentials("", "", ""));
        Result<Endpoint> endpoint = resolver.dnsService(device.userDomain(), "_ssh._tcp");

        Assert.assertFalse(endpoint.hasError());

    }
}
