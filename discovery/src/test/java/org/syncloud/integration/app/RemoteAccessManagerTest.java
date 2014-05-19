package org.syncloud.integration.app;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.app.RemoteAccessManager;
import org.syncloud.model.Device;
import org.syncloud.model.Result;

public class RemoteAccessManagerTest {

    public static final Device testDevice = new Device("192.168.1.70", 22);

    @Test
    public void testRemoteAccessSeveralTimes() {
        for(int i = 0; i < 5; i++) {
            System.out.println("Iteration: " + (i + 1));
            testRemoteAccess();
        }
    }

    public void testRemoteAccess() {
        Result<Boolean> enabled = RemoteAccessManager.isEnabled(testDevice);
        if (enabled.hasError()) {
            Assert.fail(enabled.getError());
        }

        Boolean wasEnabled = enabled.getValue();
        if (wasEnabled) {
            System.out.println("was enabled, disabling");
            Result<Boolean> disabled = RemoteAccessManager.disable(testDevice);
            if (disabled.hasError()) {
                Assert.fail(disabled.getError());
            }
            Assert.assertTrue(disabled.getValue());

            enabled = RemoteAccessManager.isEnabled(testDevice);
            if (enabled.hasError()) {
                Assert.fail(enabled.getError());
            }
            Assert.assertFalse(enabled.getValue());

        }

        System.out.println("enabling");
        Result<Device> device = RemoteAccessManager.enable(testDevice);
        if (device.hasError()) {
            Assert.fail(device.getError());
        }
        Assert.assertNotNull(device.getValue().getKey());
        Assert.assertNotNull(device.getValue().getHost());
        Assert.assertNotNull(device.getValue().getPort());
        System.out.println(device.getValue());

        System.out.println("disabling");
        Result<Boolean> disabled = RemoteAccessManager.disable(testDevice);
        if (disabled.hasError()) {
            Assert.fail(disabled.getError());
        }
        Assert.assertTrue(disabled.getValue());

        if (wasEnabled) {
            System.out.println("enabling to initial state");
            device = RemoteAccessManager.enable(testDevice);
            if (device.hasError()) {
                Assert.fail(device.getError());
            }
            Assert.assertNotNull(device.getValue().getKey());
            System.out.println(device.getValue());
        }


    }

}
