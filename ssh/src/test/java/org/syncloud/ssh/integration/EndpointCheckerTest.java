package org.syncloud.ssh.integration;

import com.google.common.base.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.EndpointChecker;
import org.syncloud.ssh.model.DirectEndpoint;

public class EndpointCheckerTest {

    @Test
    public void testDnsSrv() {
        Result<DirectEndpoint> directEndpointOptional = EndpointChecker.dnsService("testdomain1.syncloud.info", "_ssh._tcp", null);
        Assert.assertFalse(directEndpointOptional.hasError());
    }
}
