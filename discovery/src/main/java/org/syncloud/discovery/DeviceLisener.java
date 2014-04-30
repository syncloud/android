package org.syncloud.discovery;

public interface DeviceLisener {
    public void added(String url);
    public void removed(String url);

}
