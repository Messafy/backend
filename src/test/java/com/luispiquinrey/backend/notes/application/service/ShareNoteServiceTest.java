package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.ShareNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareNoteServiceTest {
    @Mock private NoteRepository repository;
    @Mock private AccountLookup accounts;
    @Mock private NoteRealtimeNotifier notifier;
    @Mock private Logger log;
    @InjectMocks private ShareNoteService service;

    @Test
    @Timeout(1)
    @Tag("shareNoteService")
    void savesSharedRepresentationBeforeNotifying() {
        Note original = prepareNote();
        Note shared = service.share("note-1", new ShareNoteCommand("public-code", "locker-password"), "owner");
        assertInstanceOf(SharedNote.class, shared);
        assertEquals(original.id(), shared.id());
        assertEquals(original.createdAt(), shared.createdAt());
        var order = inOrder(repository, notifier);
        order.verify(repository).findById(original.id());
        order.verify(repository).save(shared);
        order.verify(notifier).notifySharedNote(shared, "recipient");
        order.verifyNoMoreInteractions();
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteService")
    void failedSaveDoesNotNotifyOrMutateOriginalNote() {
        Note original = prepareNote();
        doThrow(new IllegalStateException("database unavailable")).when(repository).save(any());
        assertThrows(IllegalStateException.class,
                () -> service.share("note-1", new ShareNoteCommand("public-code", "locker-password"), "owner"));
        verifyNoInteractions(notifier);
        assertEquals("Body", original.content().content());
        assertEquals("NEW", original.status().name());
    }

    private Note prepareNote() {
        Note original = new NoteFactory().createPrivateNote("note-1", "Title", "Body", "owner");
        when(repository.findById(original.id())).thenReturn(Optional.of(original));
        when(accounts.findActiveAccountIdByPublicCode("public-code")).thenReturn(Optional.of(new UserId("recipient")));
        when(accounts.matchesLockerPassword(new UserId("recipient"), "locker-password")).thenReturn(true);
        return original;
    }
}
