package com.luispiquinrey.backend.notes.domain;

import com.luispiquinrey.backend.share.time.Date;

public class NoteFactory {

    public PrivateNote createPrivateNote(String id, String title, String content, String ownerId) {
        return new PrivateNote(id, title, content, ownerId);
    }

    public SharedNote createSharedNote(
            String id,
            String title,
            String content,
            String ownerId,
            String sharedWith
    ) {
        return new SharedNote(id, title, content, ownerId, sharedWith);
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
        return new PrivateNote(
                id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId
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
        return new SharedNote(
                id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, ownerId, sharedWith
        );
    }
}
