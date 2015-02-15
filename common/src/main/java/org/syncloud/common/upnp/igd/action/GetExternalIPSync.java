package org.syncloud.common.upnp.igd.action;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.igd.callback.GetExternalIP;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GetExternalIPSync extends GetExternalIP {

    private Logger logger = Logger.getLogger(GetExternalIPSync.class);
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private Optional<String> ip = Optional.absent();

    public GetExternalIPSync(Service service) {
        super(service);
    }

    @Override
    protected void success(String externalIPAddress) {
        ip = Optional.of(externalIPAddress);
        countDownLatch.countDown();
    }

    @Override
    public void failure(ActionInvocation invocation,
                        UpnpResponse operation,
                        String defaultMsg) {
        logger.error("operation response: " + operation.getResponseDetails());
        countDownLatch.countDown();
    }

    public Optional<String> await(long seconds) {
        try {
            countDownLatch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("interrupted: " + e.getMessage(), e);
        }

        return ip;
    }
}
