package org.syncloud.common.integration.check;

import com.google.common.base.Optional;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.junit.Test;
import org.syncloud.common.upnp.cling.ClingUPnP;
import org.syncloud.common.upnp.cling.ClingRouter;

import java.net.SocketException;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.max;
import static java.util.Collections.sort;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UPnPTest {

    public static final String MY_IP = "192.168.1.66";

    @Test
    public void test() throws InterruptedException, SocketException {
        ClingUPnP upnp = new ClingUPnP(new DefaultUpnpServiceConfiguration());
        Optional<ClingRouter> routerOptional = upnp.start().find();

        assertTrue(routerOptional.isPresent());
        ClingRouter router = routerOptional.get();
        System.out.println(router.getName());

        Optional<String> ipOpt = router.getExternalIP();
        assertTrue(ipOpt.isPresent());
        System.out.println(ipOpt.get());

        System.out.println("mappings: " + router.getPortMappingsCount());

        assertTrue(router.canToManipulatePorts(MY_IP));

        upnp.shutdown();
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
