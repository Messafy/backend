package com.luispiquinrey.backend.account.presentation.viewmodel;

public record LoginViewModel(String token) {
    @Override
    public String toString() {
        return "LoginViewModel[token=REDACTED]";
    }
}
