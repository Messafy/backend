package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.slices.create.validation.MaxLines;
import com.luispiquinrey.backend.notes.slices.create.validation.NoInvalidControlCharacters;
import com.luispiquinrey.backend.notes.slices.create.validation.NoUrls;
import jakarta.validation.constraints.*;

public record NoteCreationRequest(
        NoteType type,
        @NotNull(message = "Title cannot be null")
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title cannot be longer than 100 characters")
        @Pattern(
                regexp = "^[^\\r\\n]*$",
                message = "Title must not contain line breaks"
        ) String title,
        @NotNull(message = "Content cannot be null")
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 1000, message = "Content cannot be longer than 1000 characters")
        @MaxLines(value = 300, message = "Content cannot contain more than 300 lines")
        @NoUrls(message = "Content cannot contain URLs")
        @NoInvalidControlCharacters(message = "Content cannot contain invalid control characters")
        String content,
        @NotNull(message = "Owner ID cannot be null")
        @NotEmpty(message = "Owner ID cannot be empty")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Owner ID must be a valid 24-character hexadecimal ObjectId"
        ) String ownerId,
        @NotNull(message = "Shared-with ID cannot be null")
        @NotEmpty(message = "Shared-with ID cannot be empty")
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Shared-with ID must be a valid 24-character hexadecimal ObjectId"
        ) String sharedWith
) {
    public enum NoteType {
        PRIVATE,
        SHARED
    }
}
