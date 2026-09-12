package com.luispiquinrey.backend.notes.slices.markasread;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NoteMarkAsReadRequest(
        @NotBlank(message = "Note ID cannot be blank")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Note ID must be a valid 24-character hexadecimal ObjectId"
        ) String id
) {
}
