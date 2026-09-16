package com.luispiquinrey.backend.notes.domain;

import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;

import java.util.List;

public class PrivateNote extends Note {
    private UserId ownerId;

    PrivateNote() {}

    public PrivateNote(String id, String title, String content, String ownerId) {
        this(id, title, content, ownerId, List.of());
    }

    public PrivateNote(String id, String title, String content, String ownerId, List<Tag> tags) {
        super(id, title, content, tags);
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
        this(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, List.of(), false);
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
            String ownerId,
            List<Tag> tags,
            boolean pinned
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, tags, pinned);
        this.ownerId = new UserId(ownerId);
    }

    public UserId ownerId() {
        return ownerId;
    }

    @Override
    public boolean isOwnedBy(String ownerId) {
        return this.ownerId.equals(new UserId(ownerId));
    }

    @Override
    public String toString() {
        return "%s, ownerId=%s".formatted(super.toString(), ownerId);
    }
}
