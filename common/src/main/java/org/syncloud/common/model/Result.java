package org.syncloud.common.model;

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

    public String getErrorOrEmpty() {
        return hasError() ? getError() : "";
    }

    public String getError() {
        return error.get();
    }

    public T getValue() {
        return value.get();
    }

    public <A> Result<A> map(Function<T, A> func) {
        if (hasError())
            return Result.error(getError());
        else
            try {
                return Result.value(func.apply(getValue()));
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
    }

    public static interface Function<F, T> {
        T apply(F input) throws Exception;
    }
}
