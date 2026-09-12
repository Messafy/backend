package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.share.identity.UserId;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterAccountService {

    private static final Logger log = LoggerFactory.getLogger(RegisterAccountService.class);

    private final RegisterAccountRepository registerAccountRepository;
    private final RegisterPasswordHasher registerPasswordHasher;

    public RegisterAccountService(
            RegisterAccountRepository registerAccountRepository,
            RegisterPasswordHasher registerPasswordHasher
    ) {
        this.registerAccountRepository = registerAccountRepository;
        this.registerPasswordHasher = registerPasswordHasher;
    }

    public AccountRegisterResponse register(AccountRegisterRequest request) {
        if (request == null) {
            log.warn("Registration rejected because request body is missing");
            throw new IllegalArgumentException("request body cannot be null");
        }
        if (request.rawPassword() == null || request.rawPassword().isBlank()) {
            log.warn("Registration rejected for {} because password is missing", request.email());
            throw new IllegalArgumentException("raw password cannot be blank");
        }

        log.info("Registering account {}", request.email());

        EncodedPassword encodedPassword = registerPasswordHasher.hash(request.rawPassword());
        Account account = new Account.AccountBuilder()
                .id(new UserId(new ObjectId().toHexString()))
                .email(new Email(request.email()))
                .encodedPassword(encodedPassword)
                .build();

        registerAccountRepository.save(account);
        log.info("Registered account {} with role {}", account.id().id(), account.role());

        return AccountRegisterResponse.from(account);
    }
}
