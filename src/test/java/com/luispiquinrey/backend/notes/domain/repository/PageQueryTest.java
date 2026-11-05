package com.luispiquinrey.backend.notes.domain.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageQueryTest {

    @Test
    @Timeout(1)
    @Tag("pageQuery")
    void appliesDefaultsAndComputesTheOffset() {
        assertEquals(new PageQuery(0, PageQuery.DEFAULT_SIZE), PageQuery.of(null, null));
        assertEquals(30, new PageQuery(3, 10).offset());
        assertTrue(new PageQuery(0, 1).isPaged());
    }

    @Test
    @Timeout(1)
    @Tag("pageQuery")
    void unpagedQueriesStartAtTheBeginning() {
        assertFalse(PageQuery.unpaged().isPaged());
        assertEquals(0, PageQuery.unpaged().offset());
    }

    @Test
    @Timeout(1)
    @Tag("pageQuery")
    void rejectsNegativePagesAndOutOfRangeSizes() {
        assertThrows(IllegalArgumentException.class, () -> new PageQuery(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new PageQuery(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PageQuery(0, PageQuery.MAX_SIZE + 1));
    }
}
