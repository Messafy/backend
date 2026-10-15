package com.luispiquinrey.backend.notify.application.port.out;

public interface MailSender {

    void sendVerification(String to, String token);
}
