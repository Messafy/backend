package com.luispiquinrey.backend.account.api;

public interface VerificationTokenIssuer {

    String issue(String accountId);
}
