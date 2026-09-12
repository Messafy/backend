package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.infrastructure.AccountUserDetails;

public interface LoginTokenIssuer {
    String issue(AccountUserDetails accountUserDetails);
}
