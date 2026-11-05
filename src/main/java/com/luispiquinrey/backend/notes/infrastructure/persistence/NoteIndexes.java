package com.luispiquinrey.backend.notes.infrastructure.persistence;

final class NoteIndexes {

    static final String OWNER_STATUS = "owner_status_created";
    static final String OWNER_STATUS_KEYS = "{ 'ownerId': 1, 'status': 1, 'createdAt': -1 }";

    static final String SHARED_WITH_STATUS = "shared_with_status_created";
    static final String SHARED_WITH_STATUS_KEYS = "{ 'sharedWith': 1, 'status': 1, 'createdAt': -1 }";

    private NoteIndexes() {
    }
}
