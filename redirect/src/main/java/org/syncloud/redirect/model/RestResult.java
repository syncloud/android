package org.syncloud.redirect.model;

import com.google.common.base.Optional;

public class RestResult<T> {
    private Optional<RestError> error = Optional.absent();
    private Optional<T> value = Optional.absent();

    private RestResult(RestError error) {
        this.error = Optional.fromNullable(error);
    }

    private RestResult(T value) {
        this.value = Optional.fromNullable(value);
    }

    public static <T> RestResult<T> error(RestError error) {
        return new RestResult<T>(error);
    }

    public static <T> RestResult<T> value(T value) {
        return new RestResult<T>(value);
    }

    public boolean hasError() {
        return error.isPresent();
    }

    public RestError getError() {
        return error.get();
    }

    public T getValue() {
        return value.get();
    }
}
