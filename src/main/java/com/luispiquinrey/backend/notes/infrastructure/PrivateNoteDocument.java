package com.luispiquinrey.backend.notes.infrastructure;

import java.util.List;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notes")
@TypeAlias("private")
public class PrivateNoteDocument extends NoteDocument {

    private String ownerId;

    public PrivateNoteDocument() {
    }

    public PrivateNoteDocument(
            String id,
            String title,
            String content,
            String status,
            String createdAt,
            String readAt,
            String hiddenAt,
            String reportedAt,
            String ownerId
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt);
        this.ownerId = ownerId;
    }

    public PrivateNoteDocument(
            String id,
            String title,
            String content,
            String status,
            String createdAt,
            String readAt,
            String hiddenAt,
            String reportedAt,
            List<String> tags,
            String ownerId
    ) {
        super(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, tags);
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
