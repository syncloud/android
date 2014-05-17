package org.syncloud.integration.ssh;

import junit.framework.Assert;

import org.junit.Test;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;

import java.util.List;

public class InsiderTest {

    public static final String TEST_DEVICE = "192.168.1.65";
    public static final int TEST_PORT = 10000;

    @Test
    public void testStatus() {

        Result<List<PortMapping>> ports = Insider.listPortMappings(TEST_DEVICE);
        if (ports.hasError()) {
            Assert.fail("unable to get port list: " + ports.getError());
        }

        if (ports.getValue().contains(new PortMapping(TEST_PORT))){
            Result<SshResult> removeResult = Insider.removePort(TEST_DEVICE, TEST_PORT);
            if (ports.hasError()) {
                Assert.fail("unable to remove test port: " + removeResult.getError());
            }
        }

        Result<SshResult> addResult = Insider.addPort(TEST_DEVICE, TEST_PORT);
        if (ports.hasError()) {
            Assert.fail("unable to add test port: " + addResult.getError());
        }

        Result<SshResult> removeResult = Insider.removePort(TEST_DEVICE, TEST_PORT);
        if (ports.hasError()) {
            Assert.fail("unable to remove test port: " + removeResult.getError());
        }

    }
}
