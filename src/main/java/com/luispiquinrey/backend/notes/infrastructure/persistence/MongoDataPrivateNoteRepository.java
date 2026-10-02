package com.luispiquinrey.backend.notes.infrastructure.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MongoDataPrivateNoteRepository extends MongoRepository<PrivateNoteDocument, ObjectId> {

    @Query("{ '_class': 'private', 'ownerId': ?0, 'status': ?1 }")
    List<PrivateNoteDocument> findAllByOwnerIdAndStatus(String ownerId, String status);

    @Query("{ '_class': 'private', 'ownerId': ?0, 'status': { $in: ?1 } }")
    List<PrivateNoteDocument> findAllByOwnerIdAndStatusIn(String ownerId, List<String> statuses);
}
