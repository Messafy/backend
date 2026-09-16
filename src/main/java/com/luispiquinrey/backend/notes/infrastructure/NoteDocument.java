package com.luispiquinrey.backend.notes.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notes")
public abstract class NoteDocument {
    @Id
    private ObjectId id;

    @Version
    private Integer version;

    private String title;
    private String content;
    private String status;
    private String createdAt;
    private String readAt;
    private String hiddenAt;
    private String reportedAt;
    private List<String> tags;
    private Boolean pinned;

    public NoteDocument() {}

    public NoteDocument(String id, String title, String content, String status, String createdAt, String readAt, String hiddenAt, String reportedAt) {
        this(id, title, content, status, createdAt, readAt, hiddenAt, reportedAt, null);
    }

    public NoteDocument(String id, String title, String content, String status, String createdAt, String readAt, String hiddenAt, String reportedAt, List<String> tags) {
        this.id = new ObjectId(id);
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.hiddenAt = hiddenAt;
        this.reportedAt = reportedAt;
        this.tags = tags == null ? null : new ArrayList<>(tags);
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }

    public String getHiddenAt() {
        return hiddenAt;
    }

    public void setHiddenAt(String hiddenAt) {
        this.hiddenAt = hiddenAt;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(String reportedAt) {
        this.reportedAt = reportedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isPinned() {
        return Boolean.TRUE.equals(pinned);
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
