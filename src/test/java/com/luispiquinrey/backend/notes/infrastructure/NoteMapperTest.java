package com.luispiquinrey.backend.notes.infrastructure;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.notes.domain.PrivateNote;
import com.luispiquinrey.backend.notes.domain.SharedNote;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

public class NoteMapperTest {

    @Test
    @Timeout(1)
    @Tag("noteMapper")
    void shouldConvertToDomain() {

        PrivateNoteDocument document = new
                PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello world",
                "Body of the note",
                NoteStatus.NEW.name(),
                Instant.parse("2026-01-01T10:00:00Z").
                        toString(),
                null,
                null,
                null,
                List.of("work", "urgent"),
                "507f1f77bcf86cd799439012"
        );
        document.setVersion(1);
        NoteMapper mapper = new NoteMapper();
        PrivateNote note = (PrivateNote) mapper.toDomain(document);

        Assertions.assertNotNull(note);
        Assertions.assertInstanceOf(PrivateNote.class, note);

        Assertions.assertEquals("507f1f77bcf86cd799439011", note.
                id().id());
        Assertions.assertEquals("Hello world", note.title().title(
        ));
        Assertions.assertEquals("Body of the note", note.content()
                .content());
        Assertions.assertEquals(NoteStatus.NEW, note.status());
        Assertions.assertEquals(List.of("work", "urgent"), note.tags().stream().map(com.luispiquinrey.backend.notes.domain.Tag::tag).toList());
        Assertions.assertNotNull(note.createdAt());
        Assertions.assertNull(note.readAt());
        Assertions.assertNull(note.hiddenAt());
        Assertions.assertNull(note.reportedAt());
        Assertions.assertEquals(
                "507f1f77bcf86cd799439012",
                note.ownerId().id()
        );
    }

    @Test
    @Timeout(1)
    @Tag("noteMapper")
    void shouldConvertToDocument() {
        NoteFactory noteFactory = new NoteFactory();

        PrivateNote privateNote = noteFactory.createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello private",
                "Body of the private note",
                "507f1f77bcf86cd799439012",
                List.of(new com.luispiquinrey.backend.notes.domain.Tag("private"))
        );

        SharedNote sharedNote = noteFactory.createSharedNote(
                "507f1f77bcf86cd799439013",
                "Hello shared",
                "Body of the shared note",
                "507f1f77bcf86cd799439014",
                "507f1f77bcf86cd799439015",
                List.of(new com.luispiquinrey.backend.notes.domain.Tag("shared"))
        );

        NoteMapper mapper = new NoteMapper();

        NoteDocument privateDocument = mapper.toDocument(privateNote);
        NoteDocument sharedDocument = mapper.toDocument(sharedNote);

        Assertions.assertNotNull(privateDocument);
        Assertions.assertInstanceOf(PrivateNoteDocument.class, privateDocument);
        Assertions.assertEquals("507f1f77bcf86cd799439011", privateDocument.getId().toString());
        Assertions.assertEquals("Hello private", privateDocument.getTitle());
        Assertions.assertEquals("Body of the private note", privateDocument.getContent());
        Assertions.assertEquals(NoteStatus.NEW.name(), privateDocument.getStatus());
        Assertions.assertNotNull(privateDocument.getCreatedAt());
        Assertions.assertNull(privateDocument.getReadAt());
        Assertions.assertNull(privateDocument.getHiddenAt());
        Assertions.assertNull(privateDocument.getReportedAt());
        Assertions.assertEquals(List.of("private"), privateDocument.getTags());
        Assertions.assertEquals(
                "507f1f77bcf86cd799439012",
                ((PrivateNoteDocument) privateDocument).getOwnerId()
        );

        Assertions.assertNotNull(sharedDocument);
        Assertions.assertInstanceOf(SharedNoteDocument.class, sharedDocument);
        Assertions.assertEquals("507f1f77bcf86cd799439013", sharedDocument.getId().toString());
        Assertions.assertEquals("Hello shared", sharedDocument.getTitle());
        Assertions.assertEquals("Body of the shared note", sharedDocument.getContent());
        Assertions.assertEquals(NoteStatus.NEW.name(), sharedDocument.getStatus());
        Assertions.assertNotNull(sharedDocument.getCreatedAt());
        Assertions.assertNull(sharedDocument.getReadAt());
        Assertions.assertNull(sharedDocument.getHiddenAt());
        Assertions.assertNull(sharedDocument.getReportedAt());
        Assertions.assertEquals(List.of("shared"), sharedDocument.getTags());
        Assertions.assertEquals(
                "507f1f77bcf86cd799439014",
                ((SharedNoteDocument) sharedDocument).getOwnerId()
        );
        Assertions.assertEquals(
                "507f1f77bcf86cd799439015",
                ((SharedNoteDocument) sharedDocument).getSharedWith()
        );
    }

    @Test
    @Timeout(1)
    @Tag("noteMapper")
    void shouldThrowWhenDomainTypeIsUnknown() {
        Note unknownNote = new Note() {
            @Override
            public boolean isOwnedBy(String ownerId) {
                return false;
            }
        };
        NoteMapper mapper = new NoteMapper();

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> mapper.toDocument(unknownNote)
        );
    }
}
