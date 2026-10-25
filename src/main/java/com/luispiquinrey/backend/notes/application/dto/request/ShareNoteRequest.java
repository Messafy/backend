package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.share.identity.UserId;

import java.util.regex.Pattern;

public record ShareNoteRequest(
        String noteId,
        String requesterId,
        String recipientPublicCode,
        String recipientLockerPassword
) {
    private static final Pattern PUBLIC_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{12}$");
    private static final Pattern RAW_LOCKER_PASSWORD_PATTERN = Pattern.compile("^[\\x21-\\x7E]{8,64}$");

    public ShareNoteRequest {
        new NoteId(noteId);
        new UserId(requesterId);
        requirePublicCode(recipientPublicCode);
        requireLockerPassword(recipientLockerPassword);
    }

    private static void requirePublicCode(String publicCode) {
        if (publicCode == null || !PUBLIC_CODE_PATTERN.matcher(publicCode).matches()) {
            throw new IllegalArgumentException("recipient public code must contain exactly 12 NanoId characters");
        }
    }

    private static void requireLockerPassword(String password) {
        if (password == null || !RAW_LOCKER_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "recipient locker password must contain 8 to 64 printable ASCII characters without whitespace"
            );
        }
    }

    @Override
    public String toString() {
        return "ShareNoteRequest[recipientLockerPassword=REDACTED]";
    }
}
