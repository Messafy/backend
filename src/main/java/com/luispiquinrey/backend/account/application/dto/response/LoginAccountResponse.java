package com.luispiquinrey.backend.account.application.dto.response;

public record LoginAccountResponse(String token) {
    @Override
    public String toString() {
        return "LoginAccountResponse[token=REDACTED]";
    }
}
