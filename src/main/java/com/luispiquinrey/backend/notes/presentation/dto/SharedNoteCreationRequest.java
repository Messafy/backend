package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.application.dto.CreateSharedNoteCommand;
import com.luispiquinrey.backend.notes.presentation.validation.MaxLines;
import com.luispiquinrey.backend.notes.presentation.validation.NoInvalidControlCharacters;
import com.luispiquinrey.backend.notes.presentation.validation.NoUrls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SharedNoteCreationRequest(
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
        @NotBlank(message = "Recipient public code cannot be blank")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]{12}$",
                message = "Recipient public code must be exactly 12 NanoId characters"
        ) String recipientPublicCode,
        @NotBlank(message = "Locker password cannot be blank")
        @Size(min = 8, max = 64, message = "Locker password must contain between 8 and 64 characters")
        @Pattern(
                regexp = "^[\\x21-\\x7E]+$",
                message = "Locker password must contain only printable ASCII characters without whitespace"
        ) String recipientLockerPassword,
        List<String> tags
) {
    public CreateSharedNoteCommand toCommand() {
        return new CreateSharedNoteCommand(title, content, recipientPublicCode, recipientLockerPassword, tags);
    }
}