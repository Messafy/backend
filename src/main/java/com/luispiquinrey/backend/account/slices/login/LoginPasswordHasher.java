package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.EncodedPassword;

public interface LoginPasswordHasher {
    EncodedPassword hash(String rawPassword);

    boolean matches(String rawPassword, EncodedPassword encodedPassword);
}
