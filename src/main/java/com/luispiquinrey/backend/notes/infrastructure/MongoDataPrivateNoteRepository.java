package com.luispiquinrey.backend.notes.infrastructure;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoDataPrivateNoteRepository extends MongoRepository<PrivateNoteDocument, ObjectId> {

    List<PrivateNoteDocument> findAllByOwnerIdAndStatus(String ownerId, String status);

    List<PrivateNoteDocument> findAllByOwnerIdAndStatusIn(String ownerId, List<String> statuses);
}
