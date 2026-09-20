package com.luispiquinrey.backend.account.infrastructure.persistence;

import com.luispiquinrey.backend.account.application.port.AccountIdGenerator;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class MongoAccountIdGenerator implements AccountIdGenerator {

    @Override
    public String nextId() {
        return new ObjectId().toHexString();
    }
}
