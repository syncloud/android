package org.syncloud.android.tasks;

import com.google.common.base.Optional;

public class AsyncResult<T> {
    private Optional<T> value = Optional.absent();
    private Optional<Throwable> exception = Optional.absent();

    public AsyncResult(Optional<T> value, Optional<Throwable> exception) {
        this.value = value;
        this.exception = exception;
    }

    static public <T> AsyncResult<T> exception(Throwable exception) {
        return new AsyncResult<T>(Optional.<T>absent(), Optional.of(exception));
    }

    static public <T> AsyncResult<T> value(T value) {
        return new AsyncResult<T>(Optional.of(value), Optional.<Throwable>absent());
    }

    public boolean hasValue() {
        return value.isPresent();
    }

    public T getValue() {
        return value.get();
    }
}
