package org.syncloud.discovery;

public interface DeviceListener {
    public void added(String url);
    public void removed(String url);

}
