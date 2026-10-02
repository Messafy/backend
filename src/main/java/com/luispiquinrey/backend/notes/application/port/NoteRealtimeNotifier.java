package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.domain.model.Note;

public interface NoteRealtimeNotifier {

    void notifySharedNote(Note note, String recipientAccountId);
}
