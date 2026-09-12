package com.luispiquinrey.backend.notes.slices.delete;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record NoteDeletionRequest(@NotNull(message = "Note ID cannot be null")
                                  @NotEmpty(message = "Note ID cannot be empty")
                                  @Pattern(
                                          regexp = "^[a-fA-F0-9]{24}$",
                                          message = "Note ID must be a valid 24-character hexadecimal ObjectId"
                                  ) String id) {
}
