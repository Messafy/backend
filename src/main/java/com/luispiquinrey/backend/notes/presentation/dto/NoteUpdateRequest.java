package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.application.dto.UpdateNoteCommand;
import com.luispiquinrey.backend.notes.presentation.validation.MaxLines;
import com.luispiquinrey.backend.notes.presentation.validation.NoInvalidControlCharacters;
import com.luispiquinrey.backend.notes.presentation.validation.NoUrls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NoteUpdateRequest(
        @NotNull
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[^\\r\\n]*$")
        String title,
        @NotNull
        @NotBlank
        @Size(max = 1000)
        @MaxLines(300)
        @NoUrls
        @NoInvalidControlCharacters
        String content,
        List<String> tags
) {
    public NoteUpdateRequest(String title, String content) {
        this(title, content, null);
    }

    public UpdateNoteCommand toCommand() {
        return new UpdateNoteCommand(title, content, tags);
    }
}
