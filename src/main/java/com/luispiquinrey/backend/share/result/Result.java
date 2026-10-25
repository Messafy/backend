package com.luispiquinrey.backend.share.result;

import java.util.Objects;

public final class Result<T> {
    private final T value;
    private final ApplicationError error;

    private Result(T value, ApplicationError error) {
        this.value = value;
        this.error = error;
    }
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(ApplicationError error) {
        return new Result<>(null, Objects.requireNonNull(error, "error must not be null"));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getValue() {
        if (isFailure()) {
            throw new IllegalStateException("Cannot get value from a failed result");
        }
        return value;
    }

    public ApplicationError getError() {
        if (isSuccess()) {
            throw new IllegalStateException("Cannot get error from a successful result");
        }
        return error;
    }
}
