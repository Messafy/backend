package com.luispiquinrey.backend.notes.slices.get;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NotesBySharedWithRequest(
        @NotBlank(message = "Shared-with ID cannot be blank")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Shared-with ID must be a valid 24-character hexadecimal ObjectId"
        ) String sharedWith
) {
}
