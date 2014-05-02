package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.syncloud.model.Device;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import sun.net.util.IPAddressUtil;

@RunWith(JUnit4.class)
public class DiscoveryTest {

    private static Logger logger = LogManager.getLogger(DiscoveryTest.class.getName());

    public static final String OWN_CLOUD = "ownCloudLocal";
    private JmDNS jmdns;
    private InetAddress localHost;

    @Before
    public void setUp() throws IOException {
        logger.debug("setting up test broadcast");

        localHost = InetAddress.getLocalHost();

        jmdns = JmDNS.create(localHost);
        jmdns.registerService(ServiceInfo.create(
                Discovery.TYPE, OWN_CLOUD, 8080, 0, 0,
                new HashMap<String, String>() {{
                    put("path", "/owncloud");
                }}
        ));

        jmdns.registerService(ServiceInfo.create(
                Discovery.TYPE, OWN_CLOUD, 8081, 0, 0,
                new HashMap<String, String>() {{
                    put("path", "/owncloud");
                }}
        ));

        logger.debug("setting up test broadcast: done");
    }

    @Test
    public void testDiscovery() throws IOException {

        BlockingDeviceListener blockingDeviceListener = new BlockingDeviceListener();
        Discovery discovery = new Discovery(blockingDeviceListener, OWN_CLOUD);
        discovery.start(ByteBuffer.wrap(localHost.getAddress()).getInt());
        List<Device> devices = blockingDeviceListener.await(10, TimeUnit.SECONDS);
        discovery.stop();

        Collections.sort(devices, new Comparator<Device>() {
            @Override
            public int compare(Device device, Device device2) {
                return device.getOwnCloudUrl().compareTo(device2.getOwnCloudUrl());
            }
        });

        logger.debug(devices);
        Assert.assertEquals(devices.size(), 2);

        URL url = new URL(devices.get(0).getOwnCloudUrl());
        Assert.assertEquals("http", url.getProtocol());
        Assert.assertEquals(8080, url.getPort());
        Assert.assertEquals("/owncloud", url.getPath());
        Assert.assertTrue(IPAddressUtil.isIPv4LiteralAddress(url.getHost()));

        url = new URL(devices.get(1).getOwnCloudUrl());
        Assert.assertEquals("http", url.getProtocol());
        Assert.assertEquals(8081, url.getPort());
        Assert.assertEquals("/owncloud", url.getPath());
        Assert.assertTrue(IPAddressUtil.isIPv4LiteralAddress(url.getHost()));

    }

    @After
    public void tearDown() throws IOException {
        logger.debug("shutting down test broadcast");

        jmdns.unregisterAllServices();
        jmdns.close();

        logger.debug("shutting down test broadcast: done");
    }
}
