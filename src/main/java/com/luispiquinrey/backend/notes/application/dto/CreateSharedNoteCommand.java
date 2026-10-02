package com.luispiquinrey.backend.notes.application.dto;

import java.util.List;

public record CreateSharedNoteCommand(
        String title,
        String content,
        String recipientPublicCode,
        String recipientLockerPassword,
        List<String> tags
) {
}