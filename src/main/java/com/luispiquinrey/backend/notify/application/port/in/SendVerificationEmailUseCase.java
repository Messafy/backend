package com.luispiquinrey.backend.notify.application.port.in;

import com.luispiquinrey.backend.notify.application.dto.SendVerificationEmailRequest;

public interface SendVerificationEmailUseCase {

    void send(SendVerificationEmailRequest request);
}
