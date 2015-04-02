package org.syncloud.android.tasks;

import com.google.common.base.Optional;

public class AsyncResult<T> {
    private Optional<T> value = Optional.absent();
    private Optional<String> error = Optional.absent();

    public AsyncResult(Optional<T> value, String ifError) {
        if (value.isPresent())
            this.value = Optional.fromNullable(value.get());
        else
            this.error = Optional.fromNullable(ifError);
    }

    public AsyncResult(Optional<T> value, Optional<String> error) {
        this.value = value;
        this.error = error;
    }

    static public <T> AsyncResult<T> error(String error) {
        return new AsyncResult<T>(Optional.<T>absent(), Optional.of(error));
    }

    static public <T> AsyncResult<T> value(T value) {
        return new AsyncResult<T>(Optional.of(value), Optional.<String>absent());
    }

    public boolean hasValue() {
        return value.isPresent();
    }

    public T getValue() {
        return value.get();
    }
}
