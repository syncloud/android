package org.syncloud.common.model;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;

public class Result<T> {

    private static Logger logger = Logger.getLogger(Result.class.getName());

    public enum Void { Void }

    public static Result<Void> VOID = new Result<Void>(Void.Void);

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

    public <A> Result<A> map(Function<T, A> func) {
        if (hasError())
            return Result.error(getError());
        else
            try {
                return Result.value(func.apply(getValue()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return Result.error(e.getMessage());
            }
    }

    public <A> Result<A> flatMap(Function<T, Result<A>> func) {
        return Results.flatten(map(func));
    }

    public static interface Function<F, T> {
        T apply(F input) throws Exception;
    }
}
