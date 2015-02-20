package org.syncloud.common.upnp;

import com.google.common.base.Optional;

public interface Router {
    String getName();

    Optional<String> getExternalIP();

    int getPortMappingsCount();

    boolean canToManipulatePorts(String myIp);
}
