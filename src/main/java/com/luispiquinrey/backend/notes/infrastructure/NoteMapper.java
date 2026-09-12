package com.luispiquinrey.backend.notes.infrastructure;

import java.time.Instant;
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
                document.getOwnerId()
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
                document.getSharedWith()
        );
    }

    private PrivateNoteDocument toPrivateDocument(PrivateNote note) {
        return new PrivateNoteDocument(
                note.id().id(),
                note.title().title(),
                note.content().content(),
                note.status().name(),
                note.createdAt() == null ? null : note.createdAt().value().toString(),
                note.readAt() == null ? null : note.readAt().value().toString(),
                note.hiddenAt() == null ? null : note.hiddenAt().value().toString(),
                note.reportedAt() == null ? null : note.reportedAt().value().toString(),
                note.ownerId().id()
        );
    }

    private SharedNoteDocument toSharedDocument(SharedNote note) {
        return new SharedNoteDocument(
                note.id().id(),
                note.title().title(),
                note.content().content(),
                note.status().name(),
                note.createdAt() == null ? null : note.createdAt().value().toString(),
                note.readAt() == null ? null : note.readAt().value().toString(),
                note.hiddenAt() == null ? null : note.hiddenAt().value().toString(),
                note.reportedAt() == null ? null : note.reportedAt().value().toString(),
                note.ownerId().id(),
                note.sharedWith().ownerId().id()
        );
    }

    private Date toNoteDate(String date) {
        return date == null ? null : new Date(Instant.parse(date));
    }
}
