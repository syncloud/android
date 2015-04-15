package org.syncloud.android;

public interface Progress {
    void start();
    void stop();
    void error(String message);
    void title(String title);

    public class Empty implements Progress {

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void error(String message) {

        }

        @Override
        public void title(String title) {

        }
    }
}
