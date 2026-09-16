package com.luispiquinrey.backend.notes.infrastructure;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoDataSharedNoteRepository extends MongoRepository<SharedNoteDocument, ObjectId> {

    List<SharedNoteDocument> findAllByStatusAndSharedWith(String status, String sharedWith);

    List<SharedNoteDocument> findAllByStatusInAndSharedWith(List<String> statuses, String sharedWith);

    List<SharedNoteDocument> findAllByOwnerIdAndStatusIn(String ownerId, List<String> statuses);

    List<SharedNoteDocument> findAllByOwnerIdAndStatus(String ownerId, String status);

    List<SharedNoteDocument> findAllByStatusAndOwnerIdAndSharedWith(
            String status, String ownerId, String sharedWith
    );
}
