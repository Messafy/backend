package com.luispiquinrey.backend.notes.infrastructure.persistence;

import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class MongoNoteIdGenerator implements NoteIdGenerator {

    @Override
    public String nextId() {
        return new ObjectId().toHexString();
    }
}
