package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringBcryptPasswordHasher implements PasswordHasher {

    private static final Logger log = LoggerFactory.getLogger(SpringBcryptPasswordHasher.class);

    private final PasswordEncoder passwordEncoder;

    public SpringBcryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EncodedPassword hash(String rawPassword) {
        log.debug("Hashing password");
        return new EncodedPassword(passwordEncoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, EncodedPassword encodedPassword) {
        log.debug("Verifying password match");
        return passwordEncoder.matches(rawPassword, encodedPassword.value());
    }
}
