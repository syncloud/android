package org.syncloud.discovery;

import com.google.common.base.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

@RunWith(JUnit4.class)
public class DiscoveryTest {

    public static final String OWN_CLOUD = "ownCloudLocal";
    private JmDNS jmdns;
    private InetAddress localHost;

    @Before
    public void setUp() throws IOException {

        localHost = InetAddress.getLocalHost();

        jmdns = JmDNS.create(localHost);
        jmdns.registerService(ServiceInfo.create(
                Discovery.TYPE, OWN_CLOUD, 8080, 0, 0,
                new HashMap<String, String>() {{
                    put("path", "/owncloud");
                }}
        ));
    }

    @Test
    public void testDiscovery() throws IOException {

        int ip = ByteBuffer.wrap(localHost.getAddress()).getInt();
        Optional<String> url = Discovery.getUrl(ip, OWN_CLOUD);
        Assert.assertTrue(url.isPresent());

        Assert.assertEquals(
                "http://" + localHost.getHostName() + "-1:8080/owncloud",
                url.get());

    }

    @After
    public void tearDown() throws IOException {
        jmdns.unregisterAllServices();
        jmdns.close();
    }
}
