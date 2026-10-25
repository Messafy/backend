package com.luispiquinrey.backend.account.domain.model;

import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class AccountVersionTest {
    @Test
    @Tag("accountVersion")
    @Timeout(1)
    void newAggregateHasNoVersionAndCanRecordSuccessivePersistedRevisions() {
        Account account = builder().build();
        assertNull(account.version());
        account.recordVersion(0);
        assertEquals(0, account.version());
        account.recordVersion(1);
        assertEquals(1, account.version());
    }

    @Test
    @Tag("accountVersion")
    @Timeout(1)
    void restoredRevisionCannotBecomeNullNegativeOrMoveBackwards() {
        Account account = builder().version(7).build();
        assertThrows(IllegalArgumentException.class, () -> builder().version(-1).build());
        assertThrows(IllegalArgumentException.class, () -> account.recordVersion(null));
        assertThrows(IllegalArgumentException.class, () -> account.recordVersion(-1));
        assertThrows(IllegalArgumentException.class, () -> account.recordVersion(6));
        assertEquals(7, account.version());
    }

    private Account.AccountBuilder builder() {
        return new Account.AccountBuilder().id(new UserId("opaque-id")).email(new Email("user@example.test"))
                .encodedPassword(new EncodedPassword("encoded-for-test"))
                .lockerPassword(new LockerPassword("encoded-locker-for-test"))
                .publicCode(new PublicCode("V1StGXR8_Z5j"));
    }
}
