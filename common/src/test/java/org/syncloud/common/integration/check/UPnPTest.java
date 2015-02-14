package org.syncloud.common.integration.check;

import org.junit.Test;
import org.syncloud.common.check.UPnP;

public class UPnPTest {
    @Test
    public void test() throws InterruptedException {
        System.out.println(new UPnP().checkStatus(5000));
    }
}
