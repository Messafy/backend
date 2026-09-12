package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.EncodedPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SpringRegisterPasswordHasher implements RegisterPasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public SpringRegisterPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EncodedPassword hash(String rawPassword) {
        return new EncodedPassword(passwordEncoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, EncodedPassword encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword.value());
    }
}
