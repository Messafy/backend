package com.luispiquinrey.backend.notes.domain;

import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;

public class SharedNote extends Note {
    private UserId ownerId;
    private SharedWith sharedWith;

    SharedNote() {}

    public SharedNote(String id, String title, String content, String ownerId, String sharedWith) {
        super(id, title, content);
        this.ownerId = new UserId(ownerId);
        this.sharedWith = new SharedWith(sharedWith);
    }

    SharedNote(
            String id,
            String title,
            String content,
            NoteStatus status,
            Date createdAt,
            Date readAt,
            Date hiddenAt,
            Date reportedAt,
            String ownerId,
            String sharedWith
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt);
        this.ownerId = new UserId(ownerId);
        this.sharedWith = new SharedWith(sharedWith);
    }

    public UserId ownerId() {
        return ownerId;
    }

    public SharedWith sharedWith() {
        return sharedWith;
    }

    @Override
    public boolean isOwnedBy(String ownerId) {
        return this.ownerId.equals(new UserId(ownerId));
    }

    public boolean isSharedWith(String ownerId) {
        return this.sharedWith.equals(new SharedWith(ownerId));
    }

    @Override
    public String toString() {
        return "%s, ownerId=%s, sharedWith=%s".formatted(super.toString(), ownerId, sharedWith);
    }
}
