package com.velvetsalon.api.domain.booking;

import java.time.OffsetDateTime;

public record AvailableSlot(OffsetDateTime startTime, OffsetDateTime endTime) implements Comparable<AvailableSlot> {
    @Override
    public int compareTo(AvailableSlot other) {
        int startCompare = this.startTime.compareTo(other.startTime);
        if (startCompare != 0) {
            return startCompare;
        }
        return this.endTime.compareTo(other.endTime);
    }
}
