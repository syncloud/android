package org.syncloud.android.tasks

import android.os.AsyncTask
import org.syncloud.android.Progress
import org.syncloud.android.tasks.AsyncResult.Companion.exception
import org.syncloud.android.tasks.AsyncResult.Companion.value

class ProgressAsyncTask<TParams, TResult> @JvmOverloads constructor(
    private var progress: Progress? = null,
    private var title: String? = null,
    private var work: Work<TParams, TResult>? = null
) : AsyncTask<TParams, Void, AsyncResult<TResult>>() {
    interface Success<TResult> {
        fun run(result: TResult?)
    }

    interface Completed<TResult> {
        fun run(result: AsyncResult<TResult>?)
    }

    interface Work<TParams, TResult> {
        fun run(vararg args: TParams): TResult
    }

    private var errorMessage: String? = null
    private var success: Success<TResult>? = null
    private var completed: Completed<TResult>? = null

    override fun doInBackground(vararg args: TParams): AsyncResult<TResult>? {
        return if (work != null) {
            try {
                value(work!!.run(*args))
            } catch (th: Throwable) {
                exception(th)
            }
        } else {
            null
        }
    }

    fun setErrorMessage(message: String): ProgressAsyncTask<TParams, TResult> {
        errorMessage = message
        return this
    }

    fun setTitle(title: String): ProgressAsyncTask<TParams, TResult> {
        this.title = title
        return this
    }

    fun setProgress(progress: Progress): ProgressAsyncTask<TParams, TResult> {
        this.progress = progress
        return this
    }

    fun onSuccess(success: Success<TResult>): ProgressAsyncTask<TParams, TResult> {
        this.success = success
        return this
    }

    fun onCompleted(completed: Completed<TResult>): ProgressAsyncTask<TParams, TResult> {
        this.completed = completed
        return this
    }

    fun doWork(work: Work<TParams, TResult>): ProgressAsyncTask<TParams, TResult> {
        this.work = work
        return this
    }

    override fun onPreExecute() {
        if (progress != null) {
            progress!!.start()
            if (title != null) progress!!.title(title)
        }
    }

    override fun onPostExecute(result: AsyncResult<TResult>?) {
        if (progress != null) {
            if (result != null && !result.hasValue() && errorMessage != null) progress!!.error(
                errorMessage
            ) else progress!!.stop()
        }
        if (result == null) success!!.run(null) else if (result.hasValue() && success != null) success!!.run(
            result.getValue()
        )
        if (completed != null) completed!!.run(result)
    }

    override fun onCancelled() {
        if (progress != null) {
            progress!!.stop()
        }
    }

    companion object {
        fun execute(runnable: Runnable) {
            ProgressAsyncTask<Void, Unit>()
                .doWork(object : Work<Void, Unit> {
                    override fun run(vararg args: Void): Unit {
                        return runnable.run()
                    }
                })
                .execute()
        }
    }
}