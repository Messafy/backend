package com.luispiquinrey.backend.notes.infrastructure.persistence;

import java.util.List;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notes")
@TypeAlias("shared")
@CompoundIndex(name = NoteIndexes.OWNER_STATUS, def = NoteIndexes.OWNER_STATUS_KEYS)
@CompoundIndex(name = NoteIndexes.SHARED_WITH_STATUS, def = NoteIndexes.SHARED_WITH_STATUS_KEYS)
public class SharedNoteDocument extends NoteDocument {

    private String ownerId;
    private String sharedWith;

    public SharedNoteDocument() {
    }

    public SharedNoteDocument(
            String id,
            String title,
            String content,
            String status,
            String createdAt,
            String readAt,
            String hiddenAt,
            String reportedAt,
            String ownerId,
            String sharedWith
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt);
        this.ownerId = ownerId;
        this.sharedWith = sharedWith;
    }

    public SharedNoteDocument(
            String id,
            String title,
            String content,
            String status,
            String createdAt,
            String readAt,
            String hiddenAt,
            String reportedAt,
            List<String> tags,
            String ownerId,
            String sharedWith
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, tags);
        this.ownerId = ownerId;
        this.sharedWith = sharedWith;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(String sharedWith) {
        this.sharedWith = sharedWith;
    }
}
