package com.luispiquinrey.backend.notes.infrastructure.config;

import com.luispiquinrey.backend.notes.application.service.CreatePrivateNoteService;
import com.luispiquinrey.backend.notes.application.service.CreateSharedNoteService;
import com.luispiquinrey.backend.notes.application.service.DeleteNoteService;
import com.luispiquinrey.backend.notes.application.service.GetNoteService;
import com.luispiquinrey.backend.notes.application.service.MarkNoteAsReadService;
import com.luispiquinrey.backend.notes.application.service.PinNoteService;
import com.luispiquinrey.backend.notes.application.service.ShareNoteService;
import com.luispiquinrey.backend.notes.application.service.UpdateNoteService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CreatePrivateNoteService.class, CreateSharedNoteService.class, DeleteNoteService.class,
        GetNoteService.class, MarkNoteAsReadService.class, PinNoteService.class,
        ShareNoteService.class, UpdateNoteService.class})
public class NotesConfiguration {
}
