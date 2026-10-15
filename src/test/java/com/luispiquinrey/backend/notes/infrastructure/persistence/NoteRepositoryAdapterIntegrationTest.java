package com.luispiquinrey.backend.notes.infrastructure.persistence;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class NoteRepositoryAdapterIntegrationTest {
    @MockitoBean
    private MailSender mailSender;
    @Autowired
    private NoteRepository repository;
    @Autowired
    private MongoTemplate mongo;

    @Test
    @Timeout(5)
    @Tag("mongoNoteReplacement")
    void shouldReplacePrivateNoteWithSharedDocumentWithoutChangingId() {
        String id = new ObjectId().toHexString();
        NoteFactory factory = new NoteFactory();
        Note original = factory.createPrivateNote(id, "Title", "Body", "507f1f77bcf86cd799439012");
        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        try {
            repository.save(original);
            Note shared = factory.restoreSharedNote(id, "Title", "Body", original.status(), original.createdAt(),
                    null, null, null, "507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013",
                    original.tags(), false);
            repository.save(shared);

            SharedNote loaded = assertInstanceOf(SharedNote.class, repository.findById(original.id()).orElseThrow());
            assertEquals(shared.id(), loaded.id());
            assertEquals("507f1f77bcf86cd799439013", loaded.sharedWith().ownerId().id());
            assertEquals(original.createdAt(), loaded.createdAt());
            assertEquals(1, mongo.count(query, "notes"));

            loaded.markAsRead();
            repository.save(loaded);
            assertTrue(repository.findActiveBySharedWith("507f1f77bcf86cd799439013")
                    .stream().anyMatch(note -> note.id().equals(original.id())));
            assertTrue(repository.findActiveByOwnerAndSharedWith("507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013")
                    .stream().anyMatch(note -> note.id().equals(original.id())));
        } finally {
            mongo.remove(query, "notes");
        }
    }
}
