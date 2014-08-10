package org.syncloud.remote.integration;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.remote.RemoteAccessManager;
import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.DeviceEndpoint;

public class RemoteAccessManagerTest {

    public static final Device testDevice = new Device(null, new DeviceEndpoint("192.168.1.70", 22));

    @Test
    public void testRemoteAccessSeveralTimes() {
        for(int i = 0; i < 2; i++) {
            System.out.println("Iteration: " + (i + 1));
            testRemoteAccess();
        }
    }

    public void testRemoteAccess() {
        Result<Device> remoteDevice = RemoteAccessManager.getRemoteDevice(testDevice);
        Boolean wasEnabled = !remoteDevice.hasError();
        if (wasEnabled) {
            System.out.println(remoteDevice.getValue().getDisplayName());
            System.out.println("was enabled, disabling");
            Result<String> disabled = RemoteAccessManager.disable(testDevice);
            if (disabled.hasError()) {
                Assert.fail(disabled.getError());
            }

            remoteDevice = RemoteAccessManager.getRemoteDevice(testDevice);
            Assert.assertTrue(remoteDevice.hasError());
        }

        System.out.println("enabling");
        Result<String> enabled = RemoteAccessManager.enable(testDevice);
        if (enabled.hasError()) {
            Assert.fail(enabled.getError());
        }

        remoteDevice = RemoteAccessManager.getRemoteDevice(testDevice);
        if (remoteDevice.hasError()) {
            Assert.fail(remoteDevice.getError());
        }
        System.out.println(remoteDevice.getValue().getDisplayName());

        System.out.println("disabling");
        Result<String> disabled = RemoteAccessManager.disable(testDevice);
        if (disabled.hasError()) {
            Assert.fail(disabled.getError());
        }

        /*
        Makes no sense to enable back as key will be regenerated
        if (wasEnabled) {
            System.out.println("enabling to initial state");
            device = RemoteAccessManager.enable(testDevice);
            if (device.hasError()) {
                Assert.fail(device.getError());
            }
            Assert.assertNotNull(device.getValue().getLocalEndpoint().getKey());
            System.out.println(device.getValue());
        }*/


    }

}
