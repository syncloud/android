package org.syncloud.common.integration.check;

import com.google.common.base.Optional;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.support.model.PortMapping;
import org.junit.Test;
import org.syncloud.common.upnp.igd.Router;
import org.syncloud.common.upnp.UPnP;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class UPnPTest {
    @Test
    public void test() throws InterruptedException {
        UPnP upnp = new UPnP();
        upnp.start(new DefaultUpnpServiceConfiguration());
        Optional<Router> routerOptional = upnp.find(10);

        assertTrue(routerOptional.isPresent());
        Router router = routerOptional.get();
        System.out.println(router.getName());

        Optional<String> ipOpt = router.getExternalIP(10);
        assertTrue(ipOpt.isPresent());
        System.out.println(ipOpt.get());

        List<PortMapping> mappings = router.getPortMappings(10);
        System.out.println(mappings);

        upnp.shutdown();
    }
}
