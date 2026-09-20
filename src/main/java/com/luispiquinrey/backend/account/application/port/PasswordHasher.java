package com.luispiquinrey.backend.account.application.port;

import com.luispiquinrey.backend.account.domain.model.EncodedPassword;

public interface PasswordHasher {

    EncodedPassword hash(String rawPassword);

    boolean matches(String rawPassword, EncodedPassword encodedPassword);
}
