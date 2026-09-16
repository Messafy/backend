package com.luispiquinrey.backend.notes.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.notes.domain.PrivateNote;
import com.luispiquinrey.backend.notes.domain.SharedNote;
import com.luispiquinrey.backend.notes.domain.Tag;
import com.luispiquinrey.backend.share.time.Date;

@Primary
@Component
public class NoteMapper {

    private static final Logger log = LoggerFactory.getLogger(NoteMapper.class);

    private final NoteFactory noteFactory = new NoteFactory();

    public Note toDomain(NoteDocument document) {
        Objects.requireNonNull(document, "document cannot be null");

        if (document instanceof PrivateNoteDocument privateDocument) {
            log.debug("Mapping private note document {} to domain", document.getId());
            return toPrivateNoteDomain(privateDocument);
        }

        if (document instanceof SharedNoteDocument sharedDocument) {
            log.debug("Mapping shared note document {} to domain", document.getId());
            return toSharedNoteDomain(sharedDocument);
        }

        throw new IllegalStateException("unsupported note document type: " + document.getClass());
    }

    public NoteDocument toDocument(Note note) {
        Objects.requireNonNull(note, "note cannot be null");

        if (note instanceof PrivateNote privateNote) {
            log.debug("Mapping private note {} to MongoDB document", note.id().id());
            return toPrivateDocument(privateNote);
        }

        if (note instanceof SharedNote sharedNote) {
            log.debug("Mapping shared note {} to MongoDB document", note.id().id());
            return toSharedDocument(sharedNote);
        }

        throw new IllegalStateException("unsupported note type: " + note.getClass());
    }

    public ObjectId toObjectId(String id) {
        Objects.requireNonNull(id, "id cannot be null");
        return new ObjectId(id);
    }

    private PrivateNote toPrivateNoteDomain(PrivateNoteDocument document) {
        return noteFactory.restorePrivateNote(
                document.getId() == null ? null : document.getId().toString(),
                document.getTitle(),
                document.getContent(),
                NoteStatus.valueOf(document.getStatus()),
                toNoteDate(document.getCreatedAt()),
                toNoteDate(document.getReadAt()),
                toNoteDate(document.getHiddenAt()),
                toNoteDate(document.getReportedAt()),
                document.getOwnerId(),
                toDomainTags(document.getTags()),
                document.isPinned()
        );
    }

    private SharedNote toSharedNoteDomain(SharedNoteDocument document) {
        return noteFactory.restoreSharedNote(
                document.getId() == null ? null : document.getId().toString(),
                document.getTitle(),
                document.getContent(),
                NoteStatus.valueOf(document.getStatus()),
                toNoteDate(document.getCreatedAt()),
                toNoteDate(document.getReadAt()),
                toNoteDate(document.getHiddenAt()),
                toNoteDate(document.getReportedAt()),
                document.getOwnerId(),
                document.getSharedWith(),
                toDomainTags(document.getTags()),
                document.isPinned()
        );
    }

    private PrivateNoteDocument toPrivateDocument(PrivateNote note) {
        PrivateNoteDocument document = new PrivateNoteDocument(
                note.id().id(),
                note.title().title(),
                note.content().content(),
                note.status().name(),
                note.createdAt() == null ? null : note.createdAt().value().toString(),
                note.readAt() == null ? null : note.readAt().value().toString(),
                note.hiddenAt() == null ? null : note.hiddenAt().value().toString(),
                note.reportedAt() == null ? null : note.reportedAt().value().toString(),
                toDocumentTags(note.tags()),
                note.ownerId().id()
        );
        document.setPinned(note.pinned());
        return document;
    }

    private SharedNoteDocument toSharedDocument(SharedNote note) {
        SharedNoteDocument document = new SharedNoteDocument(
                note.id().id(),
                note.title().title(),
                note.content().content(),
                note.status().name(),
                note.createdAt() == null ? null : note.createdAt().value().toString(),
                note.readAt() == null ? null : note.readAt().value().toString(),
                note.hiddenAt() == null ? null : note.hiddenAt().value().toString(),
                note.reportedAt() == null ? null : note.reportedAt().value().toString(),
                toDocumentTags(note.tags()),
                note.ownerId().id(),
                note.sharedWith().ownerId().id()
        );
        document.setPinned(note.pinned());
        return document;
    }

    private Date toNoteDate(String date) {
        return date == null ? null : new Date(Instant.parse(date));
    }

    private List<Tag> toDomainTags(List<String> tags) {
        return tags == null ? List.of() : tags.stream().map(Tag::new).toList();
    }

    private List<String> toDocumentTags(List<Tag> tags) {
        return tags.stream().map(Tag::tag).toList();
    }
}
