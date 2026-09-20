package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.application.dto.PinNoteCommand;

public record NotePinRequest(Boolean pinned) {

    public PinNoteCommand toCommand() {
        return new PinNoteCommand(pinned);
    }
}
