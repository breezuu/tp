package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

/**
 * Represents a required start and end date-time range for an Event.
 * Guarantees: immutable; end is strictly after start.
 */
public class TimeRange {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HHmm";
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm").withResolverStyle(ResolverStyle.STRICT);
    public static final String MESSAGE_INVALID_DATETIME_FORMAT =
            "Invalid date/time format. Expected: " + DATE_TIME_PATTERN + " (e.g. 2026-03-25 0900).";
    public static final String MESSAGE_END_NOT_AFTER_START =
            "End time must be strictly after start time.";
    public static final String MESSAGE_CONSTRAINTS =
            MESSAGE_INVALID_DATETIME_FORMAT + " " + MESSAGE_END_NOT_AFTER_START;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    /**
     * Construct a {@code TimeRange}
     * @param startTimeStr A string representation of the start time
     * @param endTimeStr A string representation of the end time
     */
    public TimeRange(String startTimeStr, String endTimeStr) {
        requireNonNull(startTimeStr);
        requireNonNull(endTimeStr);
        checkArgument(isValidTimeRange(startTimeStr, endTimeStr), MESSAGE_CONSTRAINTS);
        this.startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
        this.endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * Returns true if {@code dateTimeStr} can be parsed according to {@code DATE_TIME_PATTERN}.
     */
    public static boolean isValidDateTimeFormat(String dateTimeStr) {
        try {
            LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isValidTimeRange(String startTimeStr, String endTimeStr) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);
            return endTime.isAfter(startTime);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStartTimeFormatted() {
        return startTime.format(DATE_TIME_FORMATTER);
    }

    public String getEndTimeFormatted() {
        return endTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Returns true if both timeRanges of an event are overlapping
     * @param other the reference timeRange object of another event
     * @return
     */
    public boolean isOverlapping(TimeRange other) {
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    @Override
    public String toString() {
        return String.format("%s to %s", getStartTimeFormatted(), getEndTimeFormatted());
    }

    /**
     * Return true if both timeRange is the same
     * @param other   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof TimeRange otherTimeRange) {
            return startTime.equals(otherTimeRange.startTime)
                    && endTime.equals(otherTimeRange.endTime);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
}
