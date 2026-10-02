package com.luispiquinrey.backend.account.presentation.dto;

import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterAccountRequest(String email,
                                     @NotBlank(message = "Password cannot be blank")
                                     @Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
                                     @Pattern(regexp = "^(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).+$", message = "Password must contain a number and a special character")
                                     String rawPassword,
                                     @NotBlank(message = "Locker password cannot be blank")
                                     @Size(min = 8, max = 64, message = "Locker password must contain between 8 and 64 characters")
                                     @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "Locker password must contain only printable ASCII characters without whitespace")
                                     String rawLockerPassword) {
    public RegisterAccountCommand toCommand() {
        return new RegisterAccountCommand(email, rawPassword, rawLockerPassword);
    }
}