package com.luispiquinrey.backend.notify.adapter.out.mail;

public class MailDeliveryException extends RuntimeException {

    public MailDeliveryException(Throwable cause) {
        super("Verification email could not be delivered", cause);
    }
}
