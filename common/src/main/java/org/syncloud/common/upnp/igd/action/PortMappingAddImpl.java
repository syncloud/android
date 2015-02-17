package org.syncloud.common.upnp.igd.action;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.igd.callback.PortMappingAdd;
import org.fourthline.cling.support.model.PortMapping;

public class PortMappingAddImpl extends PortMappingAdd {
    private Logger logger = Logger.getLogger(PortMappingAddImpl.class);

    private boolean successfull = false;

    public PortMappingAddImpl(PortMapping portMapping, Service service) {
        super(service, portMapping);
        logger.debug("adding: " + portMapping);
    }

    @Override
    public void success(ActionInvocation invocation) {
        successfull = true;
    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        logger.error("failed: " + operation.getResponseDetails());
        logger.error("reason: " + defaultMsg);
        successfull = false;
    }

    public boolean isSuccessfull() {
        return successfull;
    }
}
