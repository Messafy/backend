package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.application.dto.CreateNoteCommand;
import com.luispiquinrey.backend.notes.presentation.validation.MaxLines;
import com.luispiquinrey.backend.notes.presentation.validation.NoInvalidControlCharacters;
import com.luispiquinrey.backend.notes.presentation.validation.NoUrls;
import jakarta.validation.constraints.*;

import java.util.List;

public record NoteCreationRequest(
        NoteType type,
        @NotNull(message = "Title cannot be null")
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title cannot be longer than 100 characters")
        @Pattern(
                regexp = "^[^\\r\\n]*$",
                message = "Title must not contain line breaks"
        ) String title,
        @NotNull(message = "Content cannot be null")
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 1000, message = "Content cannot be longer than 1000 characters")
        @MaxLines(value = 300, message = "Content cannot contain more than 300 lines")
        @NoUrls(message = "Content cannot contain URLs")
        @NoInvalidControlCharacters(message = "Content cannot contain invalid control characters")
        String content,
        @Pattern(
                regexp = "^[a-fA-F0-9]{24}$",
                message = "Shared-with ID must be a valid 24-character hexadecimal ObjectId"
        ) String sharedWith,
        List<String> tags
) {
    public NoteCreationRequest(NoteType type, String title, String content, String sharedWith) {
        this(type, title, content, sharedWith, null);
    }

    public CreateNoteCommand toCommand() {
        return new CreateNoteCommand(
                type == null ? null : CreateNoteCommand.NoteType.valueOf(type.name()),
                title,
                content,
                sharedWith,
                tags
        );
    }

    public enum NoteType {
        PRIVATE,
        SHARED
    }
}
