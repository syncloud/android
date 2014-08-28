package org.syncloud.ssh.integration;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.EndpointVisibility;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndpointResolverTest {

    @Test
    public void testDnsSrv() {

        EndpointVisibility visibility = new EndpointVisibility();
        
        EndpointResolver resolver = new EndpointResolver(new Dns(), visibility);
        Device device = new Device(0, "",
                "testdomain1.syncloud.info",
                new DirectEndpoint("localhost", 0, "", "", ""));
        Result<DirectEndpoint> endpoint = resolver.findDirectEndpoint(device, "_ssh._tcp");

        Assert.assertFalse(endpoint.hasError());

    }
}
