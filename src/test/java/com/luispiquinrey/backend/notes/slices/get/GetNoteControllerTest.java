package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.bind.support.WebDataBinderFactory;

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439012");

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439012");

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439012");

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439012");

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439012");

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439013");

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

        MockMvc mockMvc = mockMvc(service, "507f1f77bcf86cd799439012");

        mockMvc.perform(get("/v1/notes?ownerId={ownerId}&sharedWith={sharedWith}",
                        "507f1f77bcf86cd799439012",
                        "507f1f77bcf86cd799439013"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("507f1f77bcf86cd799439014"));
    }

    private MockMvc mockMvc(GetNoteService service, String accountId) {
        AuthenticatedUser authenticatedUser = () -> accountId;
        HandlerMethodArgumentResolver authenticatedUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType() == AuthenticatedUser.class;
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return authenticatedUser;
            }
        };

        return MockMvcBuilders
                .standaloneSetup(new GetNoteController(service))
                .setCustomArgumentResolvers(authenticatedUserResolver)
                .build();
    }
}
