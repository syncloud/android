package org.syncloud.apps;

import org.syncloud.common.progress.Progress;

import static org.junit.Assert.fail;

public class FailOnErrorProgress implements Progress {
    @Override
    public void error(String error) {
        fail(error);
    }

    @Override
    public void title(String message) {}

    @Override
    public void progress(String message) {}
}
