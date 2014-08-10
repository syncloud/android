package org.syncloud.insider.integration;

import junit.framework.Assert;

import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.insider.InsiderManager;
import org.syncloud.insider.model.Endpoint;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DeviceEndpoint;

public class InsiderManagerTest {

    public static final Device testDevice = new Device(new DeviceEndpoint("192.168.1.65", 22));
    public static final int TEST_PORT = 10000;

    @Test
    public void testStatus() {

        Result<Endpoint> ports = InsiderManager.serviceInfo(testDevice, "test_service");
        if (!ports.hasError()){
            Result<String> removeResult = InsiderManager.removeService(testDevice, TEST_PORT);
            if (ports.hasError()) {
                Assert.fail("unable to remove test port: " + removeResult.getError());
            }
        }

        Result<String> addResult = InsiderManager.addService(testDevice, "ssh", "ssh", "_ssh._tcp", TEST_PORT, "");
        if (ports.hasError()) {
            Assert.fail("unable to add test port: " + addResult.getError());
        }

        Result<String> removeResult = InsiderManager.removeService(testDevice, TEST_PORT);
        if (ports.hasError()) {
            Assert.fail("unable to remove test port: " + removeResult.getError());
        }

    }
}
