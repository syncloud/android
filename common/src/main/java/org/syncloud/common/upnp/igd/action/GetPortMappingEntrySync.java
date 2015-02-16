package org.syncloud.common.upnp.igd.action;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.PortMapping;
import org.syncloud.common.upnp.igd.action.cling.GetPortMappingEntry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GetPortMappingEntrySync extends GetPortMappingEntry {

    private Logger logger = Logger.getLogger(GetExternalIPSync.class);
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private Optional<PortMapping> portMapping = Optional.absent();

    public GetPortMappingEntrySync(Service service, long index) {
        super(service, index);
    }

    @Override
    protected void success(PortMapping portMapping) {
        this.portMapping = Optional.of(portMapping);
        countDownLatch.countDown();
    }

    @Override
    public void failure(ActionInvocation invocation,
                        UpnpResponse operation,
                        String defaultMsg) {
        logger.error("operation response: " + operation.getResponseDetails());
        countDownLatch.countDown();
    }

    public Optional<PortMapping> await(long seconds) {
        try {
            countDownLatch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("interrupted: " + e.getMessage());
        }

        return portMapping;
    }
}
