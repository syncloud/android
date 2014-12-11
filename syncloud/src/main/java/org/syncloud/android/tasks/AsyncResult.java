package org.syncloud.android.tasks;

import com.google.common.base.Optional;

public class AsyncResult<T> {
    private Optional<String> error = Optional.absent();
    private Optional<T> value = Optional.absent();

    public AsyncResult(Optional<T> value, String ifError) {
        if (value.isPresent())
            this.value = Optional.fromNullable(value.get());
        else
            this.error = Optional.fromNullable(ifError);
    }
    public boolean hasError() {
        return error.isPresent();
    }

    public String getError() {
        return error.get();
    }

    public T getValue() {
        return value.get();
    }

}
