package org.syncloud.ssh;

public interface EndpointPreference {
    boolean isRemote();
    void swap();
}
