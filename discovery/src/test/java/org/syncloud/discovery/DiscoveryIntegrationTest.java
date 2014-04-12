package org.syncloud.discovery;

import com.google.common.base.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.ByteBuffer;

@RunWith(JUnit4.class)
public class DiscoveryIntegrationTest {

    @Test
    public void testDiscovery() throws IOException {

        int ip = ByteBuffer.wrap(Inet4Address.getLocalHost().getAddress()).getInt();
        Optional<String> url = Discovery.getUrl(ip, "ownCloud");
        Assert.assertTrue(url.isPresent());
        Assert.assertEquals(url.get(), "http://arm:80/owncloud");

    }
}
