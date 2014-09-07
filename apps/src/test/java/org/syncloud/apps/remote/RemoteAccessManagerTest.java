package org.syncloud.apps.remote;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.DirectEndpoint;

public class RemoteAccessManagerTest {

    public static final Device testDevice = new Device(null, null, null, new DirectEndpoint("192.168.1.70", 22, "root", "syncloud", null));
    public static final String SYNCLOUD_INFO = "syncloud.info";

    @Test
    public void testRemoteAccessSeveralTimes() {
        for(int i = 0; i < 2; i++) {
            System.out.println("Iteration: " + (i + 1));
            testRemoteAccess();
        }
    }

    public void testRemoteAccess() {
        Result<Device> remoteDevice = RemoteAccessManager.enable(testDevice, SYNCLOUD_INFO);
        Boolean wasEnabled = !remoteDevice.hasError();
        if (wasEnabled) {
            System.out.println(remoteDevice.getValue().getDisplayName());
            System.out.println("was enabled, disabling");
            Result<String> disabled = RemoteAccessManager.disable(testDevice);
            if (disabled.hasError()) {
                Assert.fail(disabled.getError());
            }

            remoteDevice = RemoteAccessManager.enable(testDevice, SYNCLOUD_INFO);
            Assert.assertTrue(remoteDevice.hasError());
        }

        remoteDevice = RemoteAccessManager.enable(testDevice, SYNCLOUD_INFO);
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
