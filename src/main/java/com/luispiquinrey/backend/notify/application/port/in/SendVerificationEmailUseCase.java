package com.luispiquinrey.backend.notify.application.port.in;

import com.luispiquinrey.backend.account.api.AccountRegistered;

public interface SendVerificationEmailUseCase {

    void handle(AccountRegistered event);
}
