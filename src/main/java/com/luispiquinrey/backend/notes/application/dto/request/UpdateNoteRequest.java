package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.Content;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.model.Title;
import com.luispiquinrey.backend.share.identity.UserId;

import java.util.List;

public record UpdateNoteRequest(
        String noteId,
        String requesterId,
        String title,
        String content,
        List<String> tags
) {
    public UpdateNoteRequest {
        new NoteId(noteId);
        new UserId(requesterId);
        new Title(title);
        new Content(content);
        tags = tags == null ? null : tags.stream().map(Tag::new).map(Tag::tag).toList();
    }
}
