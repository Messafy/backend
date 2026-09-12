package com.luispiquinrey.backend.account.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class AccountStatusTest {
    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldIdentifyActiveStatusAsActive() {
        Assertions.assertTrue(AccountStatus.ACTIVE.isActive());
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldNotIdentifyDeletedStatusAsActive() {
        Assertions.assertFalse(AccountStatus.DELETED.isActive());
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldIdentifyDeletedStatusAsFinal() {
        Assertions.assertTrue(AccountStatus.DELETED.isFinal());
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldNotIdentifyActiveStatusAsFinal() {
        Assertions.assertFalse(AccountStatus.ACTIVE.isFinal());
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldAllowTransitionFromActiveToDeleted() {
        AccountStatus active = AccountStatus.ACTIVE;
        Assertions.assertTrue(active.canTransitionTo(AccountStatus.DELETED));
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldRejectTransitionFromDeletedToActive() {
        AccountStatus deleted = AccountStatus.DELETED;
        Assertions.assertFalse(deleted.canTransitionTo(AccountStatus.ACTIVE));
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldAllowActiveStatusToRemainActive() {
        Assertions.assertTrue(AccountStatus.ACTIVE.canTransitionTo(AccountStatus.ACTIVE));
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldAllowDeletedStatusToRemainDeleted() {
        Assertions.assertTrue(AccountStatus.DELETED.canTransitionTo(AccountStatus.DELETED));
    }

    @Test
    @Timeout(1)
    @Tag("accountStatus")
    void shouldReturnStatusNameAsString() {
        Assertions.assertAll(
                () -> Assertions.assertEquals("ACTIVE", AccountStatus.ACTIVE.toString()),
                () -> Assertions.assertEquals("DELETED", AccountStatus.DELETED.toString())
        );
    }

}
