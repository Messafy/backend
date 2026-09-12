package com.luispiquinrey.backend.notes.domain;

import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;

public class PrivateNote extends Note {
    private UserId ownerId;

    PrivateNote() {}

    public PrivateNote(String id, String title, String content, String ownerId) {
        super(id, title, content);
        this.ownerId = new UserId(ownerId);
    }

    PrivateNote(
            String id,
            String title,
            String content,
            NoteStatus status,
            Date createdAt,
            Date readAt,
            Date hiddenAt,
            Date reportedAt,
            String ownerId
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt);
        this.ownerId = new UserId(ownerId);
    }

    public UserId ownerId() {
        return ownerId;
    }

    public boolean isOwnedBy(String ownerId) {
        return this.ownerId.equals(new UserId(ownerId));
    }

    @Override
    public String toString() {
        return "%s, ownerId=%s".formatted(super.toString(), ownerId);
    }
}
