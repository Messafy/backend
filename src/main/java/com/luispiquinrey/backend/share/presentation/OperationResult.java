package com.luispiquinrey.backend.share.presentation;

import java.util.Objects;
public final class OperationResult<T> {
    private final T value;
    private final ErrorViewModel error;

    private OperationResult(T value, ErrorViewModel error) {
        this.value = value;
        this.error = error;
    }

    public static <T> OperationResult<T> succeed(T value) {
        return new OperationResult<>(Objects.requireNonNull(value, "view model must not be null"), null);
    }

    public static <T> OperationResult<T> fail(ErrorViewModel error) {
        return new OperationResult<>(null, Objects.requireNonNull(error, "error must not be null"));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getValue() {
        if (isFailure()) {
            throw new IllegalStateException("Cannot get view model from a failed operation");
        }
        return value;
    }

    public ErrorViewModel getError() {
        if (isSuccess()) {
            throw new IllegalStateException("Cannot get error from a successful operation");
        }
        return error;
    }
}
