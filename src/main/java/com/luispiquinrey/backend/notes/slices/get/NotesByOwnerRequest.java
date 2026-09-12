package com.luispiquinrey.backend.notes.slices.get;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NotesByOwnerRequest(
        @NotBlank(message = "Owner ID cannot be blank")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Owner ID must be a valid 24-character hexadecimal ObjectId"
        ) String ownerId
) {
}
