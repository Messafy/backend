package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.EncodedPassword;

public interface RegisterPasswordHasher {
    EncodedPassword hash(String rawPassword);

    boolean matches(String rawPassword, EncodedPassword encodedPassword);
}
