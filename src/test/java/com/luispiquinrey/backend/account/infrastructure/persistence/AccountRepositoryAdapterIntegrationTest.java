package com.luispiquinrey.backend.account.infrastructure.persistence;

import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
import com.luispiquinrey.backend.account.domain.model.*;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import com.luispiquinrey.backend.share.identity.UserId;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountRepositoryAdapterIntegrationTest {
    @MockitoBean
    private MailSender mailSender;
    @Autowired
    private AccountRepository repository;
    @Autowired
    private MongoTemplate mongo;
    private final List<ObjectId> ids = new ArrayList<>();

    @BeforeEach
    void createRealUniqueIndexes() {
        mongo.getCollection("accounts").createIndex(Indexes.ascending("email"), new IndexOptions().unique(true).name("email"));
        mongo.getCollection("accounts").createIndex(Indexes.ascending("publicCode"), new IndexOptions().unique(true).name("public_code"));
    }

    @AfterEach
    void removeOnlyTestAccounts() {
        mongo.remove(Query.query(Criteria.where("_id").in(ids)), AccountDocument.class);
    }

    @Test
    @Tag("accountPersistenceIntegration")
    @Timeout(5)
    void translatesDuplicateEmailIntoDomainConflict() {
        Account first = account();
        repository.save(first);
        Account duplicate = account(first.email().email(), new NanoIdPublicCodeGenerator().nextCode());

        assertThrows(AccountConflictException.class, () -> repository.save(duplicate));
    }

    @Test
    @Tag("accountPersistenceIntegration")
    @Timeout(5)
    void translatesDuplicatePublicCodeIntoDomainConflict() {
        Account first = account();
        repository.save(first);
        Account duplicate = account(UUID.randomUUID() + "@example.test", first.publicCode().value());

        assertThrows(AccountConflictException.class, () -> repository.save(duplicate));
    }

    @Test
    @Tag("accountPersistenceIntegration")
    @Timeout(5)
    void staleAggregateCannotOverwriteAConcurrentDeletion() {
        Account original = account();
        repository.save(original);
        Account stale = repository.findById(original.id()).orElseThrow();
        Account latest = repository.findById(original.id()).orElseThrow();
        latest.delete();
        repository.save(latest);

        stale.verify();
        assertThrows(AccountConflictException.class, () -> repository.save(stale));
        assertEquals(AccountStatus.DELETED, repository.findById(original.id()).orElseThrow().status());
    }

    @Test
    @Tag("accountPersistenceIntegration")
    @Timeout(5)
    void successiveSavesOfTheSameAggregateAdvanceItsVersion() {
        Account account = account();
        assertNull(account.version());
        repository.save(account);
        assertEquals(0, account.version());
        account.verify();
        repository.save(account);
        assertEquals(1, account.version());
        assertEquals(1, repository.findById(account.id()).orElseThrow().version());
    }

    private Account account() {
        return account(UUID.randomUUID() + "@example.test", new NanoIdPublicCodeGenerator().nextCode());
    }

    private Account account(String email, String publicCode) {
        ObjectId id = new ObjectId();
        ids.add(id);
        return new Account.AccountBuilder().id(new UserId(id.toHexString())).email(new Email(email))
                .encodedPassword(new EncodedPassword("encoded-for-test"))
                .lockerPassword(new LockerPassword("encoded-locker-for-test"))
                .publicCode(new PublicCode(publicCode)).build();
    }
}
