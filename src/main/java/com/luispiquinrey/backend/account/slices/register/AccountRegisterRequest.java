package com.luispiquinrey.backend.account.slices.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountRegisterRequest(
        String email,
        @NotBlank(message = "Password cannot be blank")
        @Size(
                min = 8,
                max = 128,
                message = "Password must contain between 8 and 128 characters"
        )
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).+$",
                message = "Password must contain a number and a special character"
        )
        String rawPassword
) {
}
