package com.luispiquinrey.backend.notes.slices.delete;

import com.luispiquinrey.backend.notes.domain.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class DeleteNoteControllerTest {

    @Test
    @Timeout(1)
    @Tag("deleteNoteController")
    void shouldInvokeServiceWhenDeleteIsRequested() {
        DeleteNoteService service = mock(DeleteNoteService.class);
        doNothing().when(service).delete("507f1f77bcf86cd799439011");

        new DeleteNoteController(service).deleteNote(
                new NoteDeletionRequest("507f1f77bcf86cd799439011")
        );
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteController")
    void shouldPropagateNoteNotFoundFromService() {
        DeleteNoteService service = mock(DeleteNoteService.class);
        doThrow(new NoteNotFoundException("507f1f77bcf86cd799439011"))
                .when(service).delete("507f1f77bcf86cd799439011");

        DeleteNoteController controller = new DeleteNoteController(service);
        assertThrows(
                NoteNotFoundException.class,
                () -> controller.deleteNote(new NoteDeletionRequest("507f1f77bcf86cd799439011"))
        );
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteController")
    void shouldPropagateConflictFromService() {
        DeleteNoteService service = mock(DeleteNoteService.class);
        doThrow(new NoteConflictException(
                "cannot transition note from DELETED to DELETED"))
                .when(service).delete("507f1f77bcf86cd799439011");

        DeleteNoteController controller = new DeleteNoteController(service);
        assertThrows(
                NoteConflictException.class,
                () -> controller.deleteNote(new NoteDeletionRequest("507f1f77bcf86cd799439011"))
        );
    }
}
