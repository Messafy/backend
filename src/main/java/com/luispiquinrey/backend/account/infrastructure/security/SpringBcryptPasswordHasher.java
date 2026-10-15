package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SpringBcryptPasswordHasher implements PasswordHasher {

    private static final Pattern BCRYPT =
            Pattern.compile("^(?:\\{bcrypt\\})?\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private static final Logger log = LoggerFactory.getLogger(SpringBcryptPasswordHasher.class);

    private final PasswordEncoder passwordEncoder;

    public SpringBcryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EncodedPassword hash(String rawPassword) {
        log.debug("Hashing password");
        String encoded = passwordEncoder.encode(rawPassword);
        if (encoded == null || !BCRYPT.matcher(encoded).matches()) {
            throw new IllegalStateException("password encoder did not return a BCrypt hash");
        }
        return new EncodedPassword(encoded);
    }

    @Override
    public boolean matches(String rawPassword, EncodedPassword encodedPassword) {
        log.debug("Verifying password match");
        return encodedPassword != null && BCRYPT.matcher(encodedPassword.value()).matches()
                && passwordEncoder.matches(rawPassword, encodedPassword.value());
    }
}
