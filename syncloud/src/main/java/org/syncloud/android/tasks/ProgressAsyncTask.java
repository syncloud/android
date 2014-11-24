package org.syncloud.android.tasks;

import android.os.AsyncTask;

import org.syncloud.android.Progress;
import org.syncloud.common.model.Result;

public class ProgressAsyncTask<TParams, TResult> extends AsyncTask<TParams, Void, Result<TResult>> {

    public interface Completed<TResult> {
        void onCompleted(TResult result);
    }

    public interface Work<TParams, TResult> {
        Result<TResult> work(TParams... args);
    }

    private String title;
    private Progress progress;
    private Completed<TResult> completed;
    private Work<TParams, TResult> work;

    public ProgressAsyncTask(Progress progress, String title, Work<TParams, TResult> work, Completed<TResult> completed) {
        this.progress = progress;
        this.title = title;
        this.work = work;
        this.completed = completed;
    }

    public ProgressAsyncTask() {
        this(null, null, null, null);
    }

    @Override
    protected Result<TResult> doInBackground(TParams... args) {
        if (work != null)
            return work.work(args);
        else
            return null;
    }

    public ProgressAsyncTask<TParams, TResult> setTitle(String title) {
        this.title = title;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> setProgress(Progress progress) {
        this.progress = progress;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> setCompleted(Completed<TResult> completed) {
        this.completed = completed;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> setWork(Work<TParams, TResult> work) {
        this.work = work;
        return this;
    }

    @Override
    protected void onPreExecute() {
        if (progress != null) {
            progress.start();
            if (title != null)
                progress.title("Executing command");
        }
    }

    @Override
    protected void onPostExecute(Result<TResult> result) {
        if (result.hasError()) {
            if (progress != null)
                progress.error(result.getError());
        } else {
            if (progress != null)
                progress.stop();
            if (completed != null)
                completed.onCompleted(result.getValue());
        }
    }
}
