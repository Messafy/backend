package com.luispiquinrey.backend.notes.infrastructure.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDataNoteRepository extends MongoRepository<NoteDocument, ObjectId> {
}
