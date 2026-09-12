package com.luispiquinrey.backend.notes.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;

import com.luispiquinrey.backend.share.time.Date;

@AggregateRoot
public abstract class Note {
    private NoteId id;
    private Title title;
    private Content content;
    private NoteStatus status;
    private Date createdAt;
    private Date readAt;
    private Date hiddenAt;
    private Date reportedAt;

    {
        this.id = null;
        this.title = null;
        this.content = null;
        this.status = NoteStatus.NEW;
        this.createdAt = null;
        this.readAt = null;
        this.hiddenAt = null;
        this.reportedAt = null;
    }

    protected Note() {}

    protected Note(String id, String title, String content) {
        this.id = new NoteId(id);
        this.title = new Title(title);
        this.content = new Content(content);
        this.status = NoteStatus.NEW;
        this.createdAt = Date.now();
        this.readAt = null;
        this.hiddenAt = null;
        this.reportedAt = null;
    }

    protected Note(
            String id,
            String title,
            String content,
            NoteStatus status,
            Date createdAt,
            Date readAt,
            Date hiddenAt,
            Date reportedAt
    ) {
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (createdAt.value().isAfter(Date.now().value())) {
            throw new IllegalArgumentException("createdAt cannot be in the future");
        }
        if (readAt != null && readAt.value().isAfter(Date.now().value())) {
            throw new IllegalArgumentException("readAt cannot be in the future");
        }
        if (hiddenAt != null && hiddenAt.value().isAfter(Date.now().value())) {
            throw new IllegalArgumentException("hiddenAt cannot be in the future");
        }
        if (reportedAt != null && reportedAt.value().isAfter(Date.now().value())) {
            throw new IllegalArgumentException("reportedAt cannot be in the future");
        }

        this.id = new NoteId(id);
        this.title = new Title(title);
        this.content = new Content(content);
        this.status = status;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.hiddenAt = hiddenAt;
        this.reportedAt = reportedAt;
    }

    public NoteId id() {
        return id;
    }

    public Title title() {
        return title;
    }

    public void rename(String title) {
        this.title = new Title(title);
    }

    public Content content() {
        return content;
    }

    public void changeContent(String content) {
        this.content = new Content(content);
    }

    public NoteStatus status() {
        return status;
    }

    public Date createdAt() {
        return createdAt;
    }

    public void markAsRead() {
        transitionTo(NoteStatus.READ);
        this.readAt = Date.now();
    }

    public void hide() {
        transitionTo(NoteStatus.HIDDEN);
        this.hiddenAt = Date.now();
    }

    public void report() {
        transitionTo(NoteStatus.REPORTED);
        this.reportedAt = Date.now();
    }

    public void delete() {
        transitionTo(NoteStatus.DELETED);
    }

    public Date readAt() {
        return readAt;
    }

    public Date hiddenAt() {
        return hiddenAt;
    }

    public Date reportedAt() {
        return reportedAt;
    }

    private void transitionTo(NoteStatus nextStatus) {
        if (!status.canTransitionTo(nextStatus)) {
            throw new NoteConflictException(
                    "cannot transition note from %s to %s".formatted(status, nextStatus)
            );
        }

        this.status = nextStatus;
    }

    @Override
    public String toString() {
        String normalizedContent = content == null
                ? ""
                : content.content().replaceAll("\\s+", " ").trim();

        String contentPreview = normalizedContent.length() <= 40
                ? normalizedContent
                : normalizedContent.substring(0, 40) + "...";

        return "%s{id='%s', title='%s', contentPreview='%s', status=%s, createdAt=%s, readAt=%s, hiddenAt=%s, reportedAt=%s}"
                .formatted(
                        getClass().getSimpleName(),
                        id,
                        title,
                        contentPreview,
                        status,
                        createdAt,
                        readAt,
                        hiddenAt,
                        reportedAt
                );
    }

}
