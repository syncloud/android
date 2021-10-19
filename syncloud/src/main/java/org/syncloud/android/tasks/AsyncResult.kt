package org.syncloud.android.tasks

import com.google.common.base.Optional
import org.syncloud.android.tasks.AsyncResult

class AsyncResult<T>(private val value: Optional<T>, val exception: Optional<Throwable>) {
    fun hasValue(): Boolean {
        return value.isPresent
    }

    fun getValue(): T {
        return value.get()
    }

    fun getException(): Throwable {
        return exception.get()
    }

    companion object {
        @JvmStatic
        fun <T> exception(exception: Throwable): AsyncResult<T> {
            return AsyncResult(Optional.absent(), Optional.of(exception))
        }

        @JvmStatic
        fun <T> value(value: T): AsyncResult<T> {
            return AsyncResult(Optional.of(value), Optional.absent())
        }
    }
}