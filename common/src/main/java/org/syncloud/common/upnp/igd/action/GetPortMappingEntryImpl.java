package org.syncloud.common.upnp.igd.action;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.PortMapping;
import org.syncloud.common.upnp.igd.action.cling.GetPortMappingEntry;

public class GetPortMappingEntryImpl extends GetPortMappingEntry {

    private Logger logger = Logger.getLogger(GetExternalIPImpl.class);
    private Optional<PortMapping> portMapping = Optional.absent();

    public GetPortMappingEntryImpl(Service service, long index) {
        super(service, index);
    }

    @Override
    protected void success(PortMapping portMapping) {
        this.portMapping = Optional.of(portMapping);
    }

    @Override
    public void failure(ActionInvocation invocation,
                        UpnpResponse operation,
                        String defaultMsg) {
        logger.error("operation response: " + operation.getResponseDetails());
    }

    public Optional<PortMapping> getPortMapping() {
        return portMapping;
    }
}
