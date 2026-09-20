package com.luispiquinrey.backend.notify.application.domain.service;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import org.springframework.stereotype.Service;

@Service
public class SendVerificationEmailService implements SendVerificationEmailUseCase {

    private final MailSender mailSender;
    private final VerificationTokenIssuer verificationTokenIssuer;

    public SendVerificationEmailService(MailSender mailSender, VerificationTokenIssuer verificationTokenIssuer) {
        this.mailSender = mailSender;
        this.verificationTokenIssuer = verificationTokenIssuer;
    }

    @Override
    public void handle(AccountRegistered event) {
        String token = verificationTokenIssuer.issue(event.accountId());
        String link = "http://localhost:8080/v1/auth/verify?token=" + token;
        mailSender.send(
                event.email(),
                "Welcome to Messafy",
                """
                        <!--[if mso]>
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0"><tr><td>
                        <![endif]-->
                        <div style="display:none; max-height:0; overflow:hidden; opacity:0;">
                            Confirm your email to finish setting up Messafy.
                        </div>
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f5;">
                            <tr>
                                <td align="center" style="padding: 40px 16px; font-family: Arial, Helvetica, sans-serif;">
                                    <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="max-width: 480px; width: 100%; background-color: #151515; border-radius: 10px;">
                                        <tr>
                                            <td style="padding: 32px;">
                                                <h1 style="color: #fafafa; font-size: 26px; font-weight: 600; margin: 0 0 20px; font-family: Arial, Helvetica, sans-serif;">Welcome to Messafy</h1>
                                                <p style="color: #d4d4d8; font-size: 16px; line-height: 1.7; margin: 0 0 16px; font-family: Arial, Helvetica, sans-serif;">
                                                    Hi there,
                                                </p>
                                                <p style="color: #d4d4d8; font-size: 16px; line-height: 1.7; margin: 0 0 24px; font-family: Arial, Helvetica, sans-serif;">
                                                    Your account has just been created. To activate it and start writing your private and shared notes, confirm your email address with the button below.
                                                </p>
                                                <table role="presentation" cellpadding="0" cellspacing="0" style="margin: 0 0 24px;">
                                                    <tr>
                                                        <td align="center" bgcolor="#fafafa" style="border-radius: 8px;">
                                                            <a href="__LINK__" target="_blank" style="display: inline-block; padding: 12px 28px; font-size: 14px; font-weight: 600; color: #18181b; text-decoration: none; font-family: Arial, Helvetica, sans-serif; border-radius: 8px;">
                                                                Verify your account
                                                            </a>
                                                        </td>
                                                    </tr>
                                                </table>
                                                <p style="color: #a1a1aa; font-size: 14px; line-height: 1.6; margin: 0 0 28px; font-family: Arial, Helvetica, sans-serif;">
                                                    This link is valid for 24 hours. If you did not create this account, you can safely ignore this email and nothing will happen.
                                                </p>
                                                <p style="color: #71717a; font-size: 12px; margin: 0; font-family: Arial, Helvetica, sans-serif;">
                                                    Sent by Messafy. Happy note taking.
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                        <!--[if mso]>
                        </td></tr></table>
                        <![endif]-->
                """
                        .replace("__LINK__", link)
        );
    }
}