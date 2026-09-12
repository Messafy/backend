package com.luispiquinrey.backend.account.slices.get;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountGetRequest(@NotNull(message = "Account ID cannot be null")
                                @NotEmpty(message = "Account ID cannot be empty")
                                @Pattern(
                                        regexp = "^[a-fA-F0-9]{24}$",
                                        message = "Account ID must be a valid 24-character hexadecimal ObjectId"
                                ) String id) {
}
