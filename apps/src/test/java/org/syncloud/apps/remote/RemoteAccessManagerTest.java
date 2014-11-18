package org.syncloud.apps.remote;

import com.jcraft.jsch.JSch;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.apps.FailOnErrorProgress;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.ssh.Dns;
import org.syncloud.ssh.EndpointResolver;
import org.syncloud.ssh.JSchFactory;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.Endpoint;

import static org.syncloud.ssh.model.Credentials.getStandardCredentials;

public class RemoteAccessManagerTest {

    public static final Device testDevice = new Device(null, null, new Endpoint("192.168.1.70", 22), getStandardCredentials());
    public static final String SYNCLOUD_INFO = "syncloud.info";

    @Test
    public void testRemoteAccessSeveralTimes() {
        for(int i = 0; i < 2; i++) {
            System.out.println("Iteration: " + (i + 1));
            testRemoteAccess();
        }
    }

    public void testRemoteAccess() {
        Ssh ssh = new Ssh(new JSchFactory(), new EndpointResolver(new Dns()));
        FailOnErrorProgress progress = new FailOnErrorProgress();
        RemoteAccessManager accessManager = new RemoteAccessManager(new InsiderManager(ssh, progress), ssh, progress);
        Result<Device> remoteDevice = accessManager.enable(testDevice, SYNCLOUD_INFO);
        Boolean wasEnabled = !remoteDevice.hasError();
        if (wasEnabled) {
            System.out.println(remoteDevice.getValue().userDomain());
            System.out.println("was enabled, disabling");
            Result<String> disabled = accessManager.disable(testDevice);
            if (disabled.hasError()) {
                Assert.fail(disabled.getError());
            }

            remoteDevice = accessManager.enable(testDevice, SYNCLOUD_INFO);
            Assert.assertTrue(remoteDevice.hasError());
        }

        remoteDevice = accessManager.enable(testDevice, SYNCLOUD_INFO);
        if (remoteDevice.hasError()) {
            Assert.fail(remoteDevice.getError());
        }
        System.out.println(remoteDevice.getValue().userDomain());

        System.out.println("disabling");
        Result<String> disabled = accessManager.disable(testDevice);
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
            Assert.assertNotNull(device.getValue().localEndpoint().key());
            System.out.println(device.getValue());
        }*/


    }

}
