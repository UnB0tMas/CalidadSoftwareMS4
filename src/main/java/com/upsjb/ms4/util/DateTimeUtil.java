// ruta: src/main/java/com/upsjb/ms4/util/DateTimeUtil.java
package com.upsjb.ms4.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateTimeUtil {

    public static final String ZONE_LIMA_ID = "America/Lima";
    public static final ZoneId ZONE_LIMA = ZoneId.of(ZONE_LIMA_ID);

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter COMPACT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    private DateTimeUtil() {
    }

    public static Clock limaClock() {
        return Clock.system(ZONE_LIMA);
    }

    public static LocalDate today() {
        return today(limaClock());
    }

    public static LocalDate today(Clock clock) {
        return LocalDate.now(clock == null ? limaClock() : clock);
    }

    public static LocalDateTime now() {
        return now(limaClock());
    }

    public static LocalDateTime now(Clock clock) {
        return LocalDateTime.now(clock == null ? limaClock() : clock);
    }

    public static OffsetDateTime nowOffset() {
        return OffsetDateTime.now(limaClock());
    }

    public static Instant nowInstant() {
        return Instant.now(limaClock());
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return requireDate(date).atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return requireDate(date)
                .atTime(LocalTime.MAX)
                .truncatedTo(ChronoUnit.MICROS);
    }

    public static LocalDateTime startOfMonth(int year, int month) {
        return YearMonth.of(year, month)
                .atDay(1)
                .atStartOfDay();
    }

    public static LocalDateTime endOfMonth(int year, int month) {
        return YearMonth.of(year, month)
                .atEndOfMonth()
                .atTime(LocalTime.MAX)
                .truncatedTo(ChronoUnit.MICROS);
    }

    public static LocalDateTime startOfCurrentDay() {
        return startOfDay(today());
    }

    public static LocalDateTime endOfCurrentDay() {
        return endOfDay(today());
    }

    public static boolean isBetweenInclusive(LocalDateTime value,
                                             LocalDateTime from,
                                             LocalDateTime to) {
        if (value == null) {
            return false;
        }

        boolean afterFrom = from == null || !value.isBefore(from);
        boolean beforeTo = to == null || !value.isAfter(to);
        return afterFrom && beforeTo;
    }

    public static boolean isExpired(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(now());
    }

    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(now());
    }

    public static boolean isSameDay(LocalDateTime value, LocalDate date) {
        return value != null && date != null && value.toLocalDate().equals(date);
    }

    public static void requireValidRange(LocalDateTime from, LocalDateTime to, String fieldName) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException((fieldName == null || fieldName.isBlank() ? "El rango" : fieldName)
                    + " no puede tener fecha fin anterior a fecha inicio.");
        }
    }

    public static String formatDate(LocalDate date) {
        return date == null ? null : DATE_FORMAT.format(date);
    }

    public static String formatCompactDate(LocalDate date) {
        return date == null ? null : COMPACT_DATE_FORMAT.format(date);
    }

    public static String formatYear(LocalDate date) {
        return date == null ? null : YEAR_FORMAT.format(date);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMAT.format(dateTime);
    }

    public static LocalDate parseDate(String value) {
        return value == null || value.isBlank()
                ? null
                : LocalDate.parse(value.trim(), DATE_FORMAT);
    }

    public static LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank()
                ? null
                : LocalDateTime.parse(value.trim(), DATE_TIME_FORMAT);
    }

    public static LocalDateTime truncateToSeconds(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDateTime truncateToMillis(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MILLIS);
    }

    private static LocalDate requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        return date;
    }
}