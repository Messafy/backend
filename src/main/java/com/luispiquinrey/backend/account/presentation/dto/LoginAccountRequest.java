package com.luispiquinrey.backend.account.presentation.dto;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginAccountRequest(@NotNull @NotEmpty @Email String email,
                                  @NotBlank(message = "Password cannot be blank")
                                  @Size(max = 128, message = "Password cannot be longer than 128 characters") String rawPassword) {
    public LoginAccountCommand toCommand() {
        return new LoginAccountCommand(email, rawPassword);
    }
}
