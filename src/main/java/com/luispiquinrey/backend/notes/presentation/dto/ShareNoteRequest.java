package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.application.dto.ShareNoteCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShareNoteRequest(
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
        ) String recipientLockerPassword
) {
    public ShareNoteCommand toCommand() {
        return new ShareNoteCommand(recipientPublicCode, recipientLockerPassword);
    }
}
