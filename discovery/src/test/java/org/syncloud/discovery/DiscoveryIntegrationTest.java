package org.syncloud.discovery;

import com.google.common.base.Optional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;

import sun.net.util.IPAddressUtil;

@RunWith(JUnit4.class)
public class DiscoveryIntegrationTest {

    private static Logger logger = LogManager.getLogger(DiscoveryIntegrationTest.class.getName());


    @Test
    public void testDiscovery() throws IOException {

        int ip = ByteBuffer.wrap(Inet4Address.getLocalHost().getAddress()).getInt();
        Optional<String> urlString = Discovery.getUrl(ip, "ownCloud");
        Assert.assertTrue(urlString.isPresent());

        logger.debug(urlString);

        URL url = new URL(urlString.get());
        Assert.assertEquals("http", url.getProtocol());
        Assert.assertEquals(80, url.getPort());
        Assert.assertEquals("/owncloud", url.getPath());
        Assert.assertTrue(IPAddressUtil.isIPv4LiteralAddress(url.getHost()));


    }
}
