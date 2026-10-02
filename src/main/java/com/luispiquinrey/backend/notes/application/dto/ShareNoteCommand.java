package com.luispiquinrey.backend.notes.application.dto;

public record ShareNoteCommand(
        String recipientPublicCode,
        String recipientLockerPassword
) {
}
