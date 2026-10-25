package com.luispiquinrey.backend.notes.application.dto.request;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoteRequestsTest {

    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String NOTE_ID = "507f1f77bcf86cd799439011";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";
    private static final String LOCKER_PASSWORD = "Locker1!";

    @Test
    @Timeout(1)
    @Tag("noteRequests")
    void createPrivateNoteValidatesAndCopiesTags() {
        List<String> tags = new ArrayList<>(List.of("work"));

        CreatePrivateNoteRequest request = new CreatePrivateNoteRequest(
                OWNER_ID, "Title", "Content", tags);
        tags.add("later");

        assertEquals(List.of("work"), request.tags());
        assertThrows(UnsupportedOperationException.class, () -> request.tags().add("later"));
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePrivateNoteRequest(OWNER_ID, "Title", "Content", List.of("!")));
    }

    @Test
    @Timeout(1)
    @Tag("noteRequests")
    void createSharedNoteRequiresRecipientCredentials() {
        assertThrows(IllegalArgumentException.class, () -> new CreateSharedNoteRequest(
                OWNER_ID, "Title", "Content", " ", LOCKER_PASSWORD, null));
        assertThrows(IllegalArgumentException.class, () -> new CreateSharedNoteRequest(
                OWNER_ID, "Title", "Content", "bad-code", LOCKER_PASSWORD, null));
        assertThrows(IllegalArgumentException.class, () -> new CreateSharedNoteRequest(
                OWNER_ID, "Title", "Content", PUBLIC_CODE, " ", null));
        assertThrows(IllegalArgumentException.class, () -> new CreateSharedNoteRequest(
                OWNER_ID, "Title", "Content", PUBLIC_CODE, "short", null));
        assertEquals("CreateSharedNoteRequest[recipientLockerPassword=REDACTED]",
                new CreateSharedNoteRequest(
                        OWNER_ID, "Title", "Content", PUBLIC_CODE, LOCKER_PASSWORD, null).toString());
    }

    @Test
    @Timeout(1)
    @Tag("noteRequests")
    void shareNoteRequiresRecipientCredentials() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShareNoteRequest(NOTE_ID, OWNER_ID, null, LOCKER_PASSWORD));
        assertThrows(IllegalArgumentException.class,
                () -> new ShareNoteRequest(NOTE_ID, OWNER_ID, "invalid code", LOCKER_PASSWORD));
        assertThrows(IllegalArgumentException.class,
                () -> new ShareNoteRequest(NOTE_ID, OWNER_ID, PUBLIC_CODE, " "));
        assertThrows(IllegalArgumentException.class,
                () -> new ShareNoteRequest(NOTE_ID, OWNER_ID, PUBLIC_CODE, "short"));
        assertEquals("ShareNoteRequest[recipientLockerPassword=REDACTED]",
                new ShareNoteRequest(NOTE_ID, OWNER_ID, PUBLIC_CODE, LOCKER_PASSWORD).toString());
    }

    @Test
    @Timeout(1)
    @Tag("noteRequests")
    void updateNoteValidatesAndCopiesTags() {
        List<String> tags = new ArrayList<>(List.of("work"));
        UpdateNoteRequest request = new UpdateNoteRequest(NOTE_ID, OWNER_ID, "Title", "Content", tags);
        tags.add("later");

        assertEquals(List.of("work"), request.tags());
        assertThrows(UnsupportedOperationException.class, () -> request.tags().add("later"));
        assertThrows(IllegalArgumentException.class,
                () -> new UpdateNoteRequest(NOTE_ID, OWNER_ID, "Title", "Content", List.of("!")));
        assertThrows(IllegalArgumentException.class,
                () -> new UpdateNoteRequest(NOTE_ID, OWNER_ID, "Title", "Content", java.util.Arrays.asList("work", null)));
    }

    @Test
    @Timeout(1)
    @Tag("noteRequests")
    void tagCollectionsRejectNullElementsAndDoNotLeakMutableInput() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePrivateNoteRequest(OWNER_ID, "Title", "Content", java.util.Arrays.asList((String) null)));
        assertThrows(IllegalArgumentException.class,
                () -> new CreateSharedNoteRequest(
                        OWNER_ID, "Title", "Content", PUBLIC_CODE, LOCKER_PASSWORD,
                        java.util.Arrays.asList((String) null)));
    }
}
