package org.syncloud.discovery.integration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.syncloud.discovery.BlockingDeviceEndpointListener;
import org.syncloud.discovery.Discovery;
import org.syncloud.ssh.model.DirectEndpoint;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

@RunWith(JUnit4.class)
public class DiscoveryTest {

    private static Logger logger = LogManager.getLogger(DiscoveryTest.class.getName());

    public static final String TEST_SERVICE_NAME = "syncloudLocal";
    private JmDNS jmdns;
    private InetAddress localHost;

    @Before
    public void setUp() throws IOException {
        logger.debug("setting up test broadcast");

        localHost = InetAddress.getLocalHost();

        jmdns = JmDNS.create(localHost);
        jmdns.registerService(ServiceInfo.create(
                Discovery.TYPE, TEST_SERVICE_NAME, 8080, 0, 0,
                new HashMap<String, String>()
        ));

        jmdns.registerService(ServiceInfo.create(
                Discovery.TYPE, TEST_SERVICE_NAME, 8081, 0, 0,
                new HashMap<String, String>()
        ));

        logger.debug("setting up test broadcast: done");
    }

    @Test
    public void testDiscovery() throws IOException {

        BlockingDeviceEndpointListener blockingDeviceListener = new BlockingDeviceEndpointListener();
        Discovery discovery = new Discovery(blockingDeviceListener, TEST_SERVICE_NAME);
        discovery.start(ByteBuffer.wrap(localHost.getAddress()).getInt());
        List<DirectEndpoint> devices = blockingDeviceListener.await(10, TimeUnit.SECONDS);
        discovery.stop();

        Collections.sort(devices, new Comparator<DirectEndpoint>() {
            @Override
            public int compare(DirectEndpoint endpoint, DirectEndpoint endpoint1) {
                return endpoint.getPort() - endpoint1.getPort();
            }
        });

        logger.debug(devices);
        Assert.assertEquals(devices.size(), 2);

    }

    @After
    public void tearDown() throws IOException {
        logger.debug("shutting down test broadcast");

        jmdns.unregisterAllServices();
        jmdns.close();

        logger.debug("shutting down test broadcast: done");
    }
}
