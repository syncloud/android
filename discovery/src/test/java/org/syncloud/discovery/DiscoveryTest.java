package org.syncloud.discovery;

import com.google.common.base.Optional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;

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
        logger.debug("setting up test broadcast: done");
    }

    @Test
    public void testDiscovery() throws IOException {

        int ip = ByteBuffer.wrap(localHost.getAddress()).getInt();
        Optional<String> urlString = Discovery.getUrl(ip, OWN_CLOUD);

        Assert.assertTrue(urlString.isPresent());

        logger.debug(urlString.get());

        URL url = new URL(urlString.get());
        Assert.assertEquals("http", url.getProtocol());
        Assert.assertEquals(8080, url.getPort());
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
