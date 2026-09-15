package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetNoteControllerTest {

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnNoteWhenExists() throws Exception {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        GetNoteService service = mock(GetNoteService.class);
        when(service.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(note));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes/{id}", "507f1f77bcf86cd799439011"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.type").value("PRIVATE"))
                .andExpect(jsonPath("$.title").value("Hello"));
    }

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnNotFoundWhenMissing() throws Exception {
        GetNoteService service = mock(GetNoteService.class);
        when(service.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.empty());

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes/{id}", "507f1f77bcf86cd799439011"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnEmptyListWhenNoActiveByOwner() throws Exception {
        GetNoteService service = mock(GetNoteService.class);
        when(service.findActiveByOwner("507f1f77bcf86cd799439012"))
                .thenReturn(List.of());

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes?ownerId={ownerId}", "507f1f77bcf86cd799439012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnListWhenFilteringByStatus() throws Exception {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        GetNoteService service = mock(GetNoteService.class);
        when(service.findByStatus(NoteStatus.NEW)).thenReturn(List.of(note));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes?status={status}", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("507f1f77bcf86cd799439011"));
    }

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnListWhenOwnerHasActiveNotes() throws Exception {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        GetNoteService service = mock(GetNoteService.class);
        when(service.findActiveByOwner("507f1f77bcf86cd799439012"))
                .thenReturn(List.of(note));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes?ownerId={ownerId}", "507f1f77bcf86cd799439012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$[0].title").value("Hello"));
    }

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnListWhenFilteringBySharedWith() throws Exception {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439014",
                "Shared",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        GetNoteService service = mock(GetNoteService.class);
        when(service.findActiveBySharedWith("507f1f77bcf86cd799439013"))
                .thenReturn(List.of(note));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes?sharedWith={sharedWith}", "507f1f77bcf86cd799439013"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("507f1f77bcf86cd799439014"))
                .andExpect(jsonPath("$[0].type").value("SHARED"));
    }

    @Test
    @Timeout(3)
    @Tag("getNoteController")
    void shouldReturnListWhenFilteringByOwnerAndSharedWith() throws Exception {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439014",
                "Shared",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        GetNoteService service = mock(GetNoteService.class);
        when(service.findActiveByOwnerAndSharedWith(
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        )).thenReturn(List.of(note));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .build();

        mockMvc.perform(get("/v1/notes?ownerId={ownerId}&sharedWith={sharedWith}",
                        "507f1f77bcf86cd799439012",
                        "507f1f77bcf86cd799439013"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("507f1f77bcf86cd799439014"));
    }
}
