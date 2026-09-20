package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.service.VerifyAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/v1/auth")
public class VerifyAccountController {

    private final VerifyAccountService verifyAccountService;
    private final String frontendUrl;

    public VerifyAccountController(
            VerifyAccountService verifyAccountService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.verifyAccountService = verifyAccountService;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/verify")
    public RedirectView verify(@RequestParam("token") String token) {
        try {
            verifyAccountService.verifyByToken(token);
            return new RedirectView(frontendUrl + "/login?verification=success", false);
        } catch (RuntimeException e) {
            return new RedirectView(frontendUrl + "/login?verification=failed", false);
        }
    }
}
