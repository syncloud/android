package org.syncloud.common.model;

//import org.fourthline.cling.support.model.PortMapping;

import java.util.List;

public class UPnPStatus {
    public String routerName;
    public String externalAddress;

    public UPnPStatus(String routerName, String externalAddress) {
        this.routerName = routerName;
        this.externalAddress = externalAddress;
    }

    @Override
    public String toString() {
        return "UPnPStatus{" +
                "routerName='" + routerName + '\'' +
                ", externalAddress='" + externalAddress + '\'' +
                '}';
    }
}
