package com.luispiquinrey.backend.notes.slices.get;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NotesByOwnerAndSharedWithRequest(
        @NotBlank(message = "Owner ID cannot be blank")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Owner ID must be a valid 24-character hexadecimal ObjectId"
        ) String ownerId,
        @NotBlank(message = "Shared-with ID cannot be blank")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Shared-with ID must be a valid 24-character hexadecimal ObjectId"
        ) String sharedWith
) {
}
