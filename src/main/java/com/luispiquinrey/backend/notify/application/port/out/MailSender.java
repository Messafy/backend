package com.luispiquinrey.backend.notify.application.port.out;

public interface MailSender {

    void send(String to, String subject, String body);
}
