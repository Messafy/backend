package com.luispiquinrey.backend.share.presentation;

import java.util.Objects;

public record ErrorViewModel(String message, String code) {
    public ErrorViewModel {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(code, "code must not be null");
        if (message.isBlank() || code.isBlank()) {
            throw new IllegalArgumentException("message and code must not be blank");
        }
    }
}
