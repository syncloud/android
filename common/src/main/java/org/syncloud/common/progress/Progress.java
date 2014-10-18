package org.syncloud.common.progress;

public interface Progress {
    public void error(String error);
    public void title(String message);
    public void progress(String message);
}
