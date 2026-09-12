package com.luispiquinrey.backend.account.infrastructure;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoDataAccountRepository extends MongoRepository<AccountDocument, ObjectId> {
    Optional<AccountDocument> findByEmail(String email);
}
