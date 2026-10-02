package com.luispiquinrey.backend.notes.infrastructure.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MongoDataSharedNoteRepository extends MongoRepository<SharedNoteDocument, ObjectId> {

    List<SharedNoteDocument> findAllByStatusAndSharedWith(String status, String sharedWith);

    List<SharedNoteDocument> findAllByStatusInAndSharedWith(List<String> statuses, String sharedWith);

    @Query("{ '_class': 'shared', 'ownerId': ?0, 'status': { $in: ?1 } }")
    List<SharedNoteDocument> findAllByOwnerIdAndStatusIn(String ownerId, List<String> statuses);

    @Query("{ '_class': 'shared', 'ownerId': ?0, 'status': ?1 }")
    List<SharedNoteDocument> findAllByOwnerIdAndStatus(String ownerId, String status);

    List<SharedNoteDocument> findAllByStatusAndOwnerIdAndSharedWith(
            String status, String ownerId, String sharedWith
    );
}
