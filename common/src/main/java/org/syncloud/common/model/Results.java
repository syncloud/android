package org.syncloud.common.model;

public class Results {
    public static <T> Result<T> flatten(Result<Result<T>> input) {
        if (input.hasError())
            return Result.error(input.getError());
        else
            return input.getValue();
    }
}
