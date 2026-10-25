package com.luispiquinrey.backend.account.application.port;

public interface VerificationTokenCodec {
    String issue(String accountId);
    String extractAccountId(String rawToken);
}
