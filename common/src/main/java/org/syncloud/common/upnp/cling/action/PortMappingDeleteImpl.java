package org.syncloud.common.upnp.cling.action;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.igd.callback.PortMappingDelete;
import org.fourthline.cling.support.model.PortMapping;

public class PortMappingDeleteImpl extends PortMappingDelete {
    private Logger logger = Logger.getLogger(PortMappingAddImpl.class);

    private boolean successfull = false;

    public PortMappingDeleteImpl(PortMapping portMapping, Service service) {
        super(service, portMapping);
        logger.debug("removing: " + portMapping);
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
