package org.syncloud.common.integration.check;

import com.google.common.base.Optional;

import org.junit.Test;
import org.syncloud.common.upnp.Router;
import org.syncloud.common.upnp.UPnP;

import java.net.SocketException;

import static org.junit.Assert.assertTrue;

public class UPnPTest {

    public static final String MY_IP = "192.168.1.66";

    @Test
    public void test() throws InterruptedException, SocketException {
        Optional<Router> routerOptional = new UPnP().find();

        assertTrue(routerOptional.isPresent());
        Router router = routerOptional.get();
        System.out.println(router.getName());

        Optional<String> ipOpt = router.getExternalIP();
        assertTrue(ipOpt.isPresent());
        System.out.println(ipOpt.get());

        System.out.println("mappings: " + router.getPortMappingsCount());

        assertTrue(router.canManipulatePorts(MY_IP));

    }

    /*private Optional<InetAddress> findMyIp() throws SocketException {
        return FluentIterable.from(Collections.list(NetworkInterface.getNetworkInterfaces()))
                .transformAndConcat(new Function<NetworkInterface, Iterable<InetAddress>>() {
                    @Override
                    public Iterable<InetAddress> apply(NetworkInterface input) {
                        return Collections.list(input.getInetAddresses());
                    }
                })
                .firstMatch(new Predicate<InetAddress>() {
                    @Override
                    public boolean apply(InetAddress input) {
                        return input.getHostAddress().startsWith("192");
                    }
                });
    }*/


}
