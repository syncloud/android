package org.syncloud.model;

import com.google.common.base.Optional;

public class Result<T> {
    private Optional<String> error = Optional.absent();
    private Optional<T> value = Optional.absent();

    private Result(String error) {
        this.error = Optional.fromNullable(error);
    }

    private Result(T value) {
        this.value = Optional.fromNullable(value);
    }

    public static <T> Result<T> error(String error) {
        return new Result<T>(error);
    }

    public static <T> Result<T> value(T value) {
        return new Result<T>(value);
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
