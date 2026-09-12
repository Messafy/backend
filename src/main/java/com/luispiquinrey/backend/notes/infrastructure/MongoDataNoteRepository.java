package com.luispiquinrey.backend.notes.infrastructure;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoDataNoteRepository extends MongoRepository<NoteDocument, ObjectId> {

    List<NoteDocument> findAllByStatus(String status);
}
