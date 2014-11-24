package org.syncloud.android;

public interface Progress {
    void start();
    void stop();
    void error(String message);
    void title(String title);
}
