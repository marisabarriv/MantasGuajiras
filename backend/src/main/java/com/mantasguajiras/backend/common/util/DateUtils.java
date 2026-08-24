package com.mantasguajiras.backend.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    public static boolean isBetween(
            LocalDateTime dateTime,
            LocalDateTime start,
            LocalDateTime end) {

        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
}