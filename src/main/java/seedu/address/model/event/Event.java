package seedu.address.model.event;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an Event in the address book.
 */
public class Event {

    // Identify fields
    private final Title title;
    private final TimeRange timeRange;

    // Fields required for relations with Person
    private final int eventId;
    private int numberOfPersonLinked;

    // Optional Data fields
    private final Optional<Description> description;



    /**
     * Creates an Event. Title and time range are required. Description is optional.
     * {@code numberOfPersonLinked} starts at 1 for a newly created event.
     */
    public Event(Title title, Optional<Description> description, TimeRange timeRange) {
        requireAllNonNull(title, description, timeRange);
        this.title = title;
        this.description = description;
        this.timeRange = timeRange;
        this.numberOfPersonLinked = 1;
        this.eventId = Objects.hash(this.title, this.timeRange);
    }

    public Title getTitle() {
        return title;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public int getEventId() {
        return eventId;
    }

    public int getNumberOfPersonLinked() {
        return numberOfPersonLinked;
    }

    public Optional<Description> getDescription() {
        return description;
    }

    /**
     * Increments the count of linked persons.
     */
    public void incrementNumberOfPersonLinked() {
        numberOfPersonLinked += 1;
    }

    /**
     * Decrements the count of linked persons.
     * Throws if the count would go below zero.
     */
    public void decrementNumberOfPersonLinked() {
        if (numberOfPersonLinked <= 0) {
            throw new IllegalStateException("numberOfPersonLinked cannot be negative.");
        }
        numberOfPersonLinked -= 1;
    }

    public String getStartTimeFormatted() {
        return timeRange.getStartTimeFormatted();
    }

    public String getEndTimeFormatted() {
        return timeRange.getEndTimeFormatted();
    }

    /**
     * Returns true if both event is the same
     * We define a event to be uniquely identified by event identifier
     */
    public boolean isSameEvent(Event otherEvent) {
        if (otherEvent == this) {
            return true;
        }

        return otherEvent != null
                && otherEvent.title.equals(title)
                && otherEvent.timeRange.equals(timeRange);
    }

    /**
     * Returns true if both event's time range are overlapping
     */
    public boolean isClashingWith(Event otherEvent) {
        return timeRange.isOverlapping(otherEvent.timeRange);
    }

    @Override
    public String toString() {
        String duration = "From " + timeRange;
        if (description.isPresent()) {
            return String.format("Title: %s, Description: %s, Duration: %s",
                    title, description.get(), duration);
        }
        return String.format("Title: %s, Duration: %s", title, duration);
    }

    /**
     * Return true if both Event have the same title and timeRange
     * @param other   the reference object with which to compare.
     * @return True if the events have the same title and timeRange, else return false
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Event otherEvent) {
            return title.equals(otherEvent.title)
                    && timeRange.equals(otherEvent.timeRange);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.description, this.timeRange);
    }
}
