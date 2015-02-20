package org.syncloud.common.upnp.cling.action;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.igd.callback.GetExternalIP;

public class GetExternalIPImpl extends GetExternalIP {

    private Logger logger = Logger.getLogger(GetExternalIPImpl.class);
    private Optional<String> ip = Optional.absent();

    public GetExternalIPImpl(Service service) {
        super(service);
    }

    @Override
    protected void success(String externalIPAddress) {
        ip = Optional.of(externalIPAddress);
    }

    @Override
    public void failure(ActionInvocation invocation,
                        UpnpResponse operation,
                        String defaultMsg) {
        logger.error("operation response: " + operation.getResponseDetails());
    }

    public Optional<String> getIp() {
        return ip;
    }
}
