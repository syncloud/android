package org.syncloud.integration.ssh;

import com.google.common.base.Optional;

import junit.framework.Assert;

import org.junit.Test;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.app.InsiderManager;

import java.util.List;

public class InsiderManagerTest {

    public static final String TEST_DEVICE = "192.168.1.65";
    public static final int TEST_PORT = 10000;

    @Test
    public void testStatus() {

        Result<Optional<PortMapping>> ports = InsiderManager.localPortMapping(TEST_DEVICE, TEST_PORT);
        if (ports.hasError()) {
            Assert.fail("unable to get port list: " + ports.getError());
        }

        if (ports.getValue().isPresent()){
            Result<SshResult> removeResult = InsiderManager.removePort(TEST_DEVICE, TEST_PORT);
            if (ports.hasError()) {
                Assert.fail("unable to remove test port: " + removeResult.getError());
            }
        }

        Result<SshResult> addResult = InsiderManager.addPort(TEST_DEVICE, TEST_PORT);
        if (ports.hasError()) {
            Assert.fail("unable to add test port: " + addResult.getError());
        }

        Result<SshResult> removeResult = InsiderManager.removePort(TEST_DEVICE, TEST_PORT);
        if (ports.hasError()) {
            Assert.fail("unable to remove test port: " + removeResult.getError());
        }

    }
}
