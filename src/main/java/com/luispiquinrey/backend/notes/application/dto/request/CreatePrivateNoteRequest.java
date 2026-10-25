package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.Content;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.model.Title;
import com.luispiquinrey.backend.share.identity.UserId;

import java.util.List;

public record CreatePrivateNoteRequest(
        String ownerId,
        String title,
        String content,
        List<String> tags
) {
    public CreatePrivateNoteRequest {
        new UserId(ownerId);
        new Title(title);
        new Content(content);
        tags = tags == null ? null : tags.stream().map(Tag::new).map(Tag::tag).toList();
    }
}
