package com.luispiquinrey.backend.notes.domain;

import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;

import java.util.List;

public class SharedNote extends Note {
    private UserId ownerId;
    private SharedWith sharedWith;

    SharedNote() {}

    public SharedNote(String id, String title, String content, String ownerId, String sharedWith) {
        this(id, title, content, ownerId, sharedWith, List.of());
    }

    public SharedNote(String id, String title, String content, String ownerId, String sharedWith, List<Tag> tags) {
        super(id, title, content, tags);
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
        this(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, sharedWith, List.of(), false);
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
            String sharedWith,
            List<Tag> tags,
            boolean pinned
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, tags, pinned);
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
