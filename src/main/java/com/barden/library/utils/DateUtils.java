package com.barden.library.utils;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Date utils class.
 */
public final class DateUtils {

    /**
     * Converts local date to date.
     *
     * @param date Local date.
     * @return Date.
     */
    @Nonnull
    public static Date of(@Nonnull LocalDate date) {
        return Date.from(Objects.requireNonNull(date, "local date cannot be null!").atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Formats date.
     *
     * @param date Date.
     * @return Formatted date.
     */
    @Nonnull
    public static String format(@Nonnull Date date) {
        return new SimpleDateFormat("MM/dd/yyyy").format(Objects.requireNonNull(date, "date cannot be null!"));
    }
}
