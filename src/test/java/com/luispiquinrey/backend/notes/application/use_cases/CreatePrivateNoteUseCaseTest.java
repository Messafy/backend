package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.CreatePrivateNoteRequest;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ErrorType;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePrivateNoteUseCaseTest {

    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String NOTE_ID = "507f1f77bcf86cd799439011";

    @Mock
    private NoteRepository repository;

    @Mock
    private NoteIdGenerator idGenerator;

    @Mock
    private Logger log;

    @InjectMocks
    private CreatePrivateNoteUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteUseCase")
    void createsPrivateNoteAndReturnsCreatedResponse() {
        when(idGenerator.nextId()).thenReturn(NOTE_ID);
        var request = new CreatePrivateNoteRequest(OWNER_ID, "Hello", "Body", List.of("work", "urgent"));

        Result<?> result = useCase.create(request);

        assertTrue(result.isSuccess());
        var response = (com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse) result.getValue();
        assertEquals(NOTE_ID, response.id());
        assertEquals("PRIVATE", response.type());
        assertEquals("NEW", response.status());
        assertNotNull(response.createdAt());

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        Note saved = captor.getValue();
        assertInstanceOf(PrivateNote.class, saved);
        assertEquals("Hello", saved.title().title());
        assertEquals("Body", saved.content().content());
        assertEquals(List.of("work", "urgent"), saved.tags().stream().map(com.luispiquinrey.backend.notes.domain.model.Tag::tag).toList());
        assertTrue(saved.isOwnedBy(OWNER_ID));
        assertEquals(NoteStatus.NEW, saved.status());
    }

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteUseCase")
    void createsPrivateNoteWithoutTags() {
        when(idGenerator.nextId()).thenReturn(NOTE_ID);
        var request = new CreatePrivateNoteRequest(OWNER_ID, "Hello", "Body", null);

        var result = useCase.create(request);

        assertTrue(result.isSuccess());
        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().tags().isEmpty());
    }

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteUseCase")
    void rejectsNullRequestWithoutSaving() {
        var result = useCase.create(null);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        assertEquals("note creation data cannot be null", result.getError().message());
    }

    @Test
    @Timeout(1)
    @Tag("createPrivateNoteUseCase")
    void requestValidationRejectsInvalidDomainDataBeforeTheUseCaseRuns() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePrivateNoteRequest(OWNER_ID, " ", "Body", null));
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePrivateNoteRequest(OWNER_ID, "Title", "https://example.com", null));
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePrivateNoteRequest(" ", "Title", "Body", null));
    }
}
