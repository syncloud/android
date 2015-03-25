package org.syncloud.platform.ssh;

public interface EndpointPreference {
    boolean isRemote();
    void swap();
}
