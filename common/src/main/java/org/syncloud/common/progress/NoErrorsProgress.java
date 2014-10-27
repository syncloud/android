package org.syncloud.common.progress;

public class NoErrorsProgress implements Progress {

    private Progress progress;

    public NoErrorsProgress(Progress progress) {
        this.progress = progress;
    }

    @Override
    public void error(String error) {
        //no error
    }

    @Override
    public void title(String message) {
        progress.title(message);
    }

    @Override
    public void progress(String message) {
        progress.progress(message);
    }
}
