package com.luispiquinrey.backend.account.application.dto.response;

import com.luispiquinrey.backend.account.domain.model.Account;

public record VerifyAccountResponse(boolean verified) {
    public static VerifyAccountResponse from(Account account) {
        return new VerifyAccountResponse(account.isVerified());
    }
}
