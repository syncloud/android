package org.syncloud.common.upnp;

import com.google.common.base.Optional;

public interface UPnP {
    Optional<? extends Router> find();

    void start();
    void shutdown();
}
