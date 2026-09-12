package com.luispiquinrey.backend.notes.domain;

import java.util.EnumSet;
import java.util.Set;

public enum NoteStatus {
    NEW,
    READ,
    HIDDEN,
    REPORTED,
    DELETED;

    private static final Set<NoteStatus> VISIBLE_STATUSES = EnumSet.of(NEW, READ);
    private static final Set<NoteStatus> FINAL_STATUSES = EnumSet.of(DELETED);

    public boolean isVisible() {
        return VISIBLE_STATUSES.contains(this);
    }

    public boolean isFinal() {
        return FINAL_STATUSES.contains(this);
    }

    public boolean canTransitionTo(NoteStatus nextStatus) {
        if (this == nextStatus) {
            return true;
        }

        if (isFinal()) {
            return false;
        }

        return switch (this) {
            case NEW -> nextStatus == READ
                    || nextStatus == HIDDEN
                    || nextStatus == REPORTED
                    || nextStatus == DELETED;
            case READ -> nextStatus == HIDDEN
                    || nextStatus == REPORTED
                    || nextStatus == DELETED;
            case HIDDEN -> nextStatus == DELETED;
            case REPORTED -> nextStatus == HIDDEN
                    || nextStatus == DELETED;
            case DELETED -> false;
        };
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}
