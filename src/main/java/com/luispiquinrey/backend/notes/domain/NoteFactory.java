package com.luispiquinrey.backend.notes.domain;

import com.luispiquinrey.backend.share.time.Date;

import java.util.List;

public class NoteFactory {

    public PrivateNote createPrivateNote(String id, String title, String content, String ownerId) {
        return createPrivateNote(id, title, content, ownerId, List.of());
    }

    public PrivateNote createPrivateNote(String id, String title, String content, String ownerId, List<Tag> tags) {
        return new PrivateNote(id, title, content, ownerId, tags);
    }

    public SharedNote createSharedNote(
            String id,
            String title,
            String content,
            String ownerId,
            String sharedWith
    ) {
        return createSharedNote(id, title, content, ownerId, sharedWith, List.of());
    }

    public SharedNote createSharedNote(
            String id,
            String title,
            String content,
            String ownerId,
            String sharedWith,
            List<Tag> tags
    ) {
        return new SharedNote(id, title, content, ownerId, sharedWith, tags);
    }

    public PrivateNote restorePrivateNote(
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
        return restorePrivateNote(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, List.of());
    }

    public PrivateNote restorePrivateNote(
            String id,
            String title,
            String content,
            NoteStatus status,
            Date createdAt,
            Date readAt,
            Date hiddenAt,
            Date reportedAt,
            String ownerId,
            List<Tag> tags
    ) {
        return restorePrivateNote(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, tags, false);
    }

    public PrivateNote restorePrivateNote(
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
        return new PrivateNote(
                id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, tags, pinned
        );
    }

    public SharedNote restoreSharedNote(
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
        return restoreSharedNote(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, sharedWith, List.of());
    }

    public SharedNote restoreSharedNote(
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
            List<Tag> tags
    ) {
        return restoreSharedNote(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, sharedWith, tags, false);
    }

    public SharedNote restoreSharedNote(
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
        return new SharedNote(
                id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, sharedWith, tags, pinned
        );
    }
}
