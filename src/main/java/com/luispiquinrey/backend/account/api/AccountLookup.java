package com.luispiquinrey.backend.account.api;

import com.luispiquinrey.backend.share.identity.UserId;

import java.util.Optional;

public interface AccountLookup {
    boolean existsActiveAccount(String accountId);

    Optional<UserId> findActiveAccountIdByPublicCode(String publicCode);

    boolean matchesLockerPassword(UserId accountId, String rawLockerPassword);
}