package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class EventTest {

    private static final Title TITLE_MEETING = new Title("Team Meeting");
    private static final Title TITLE_REVIEW = new Title("Project Review");
    private static final Description DESC_A = new Description("Discuss milestones");
    private static final Description DESC_B = new Description("Different notes");

    private static TimeRange timeRange1() {
        return new TimeRange("2026-03-25 0900", "2026-03-25 1000");
    }

    private static TimeRange timeRange2() {
        return new TimeRange("2026-03-26 0900", "2026-03-26 1000");
    }

    @Test
    public void constructor_validFields_success() {
        Event event = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        assertEquals(TITLE_MEETING, event.getTitle());
        assertEquals(Optional.of(DESC_A), event.getDescription());
        assertEquals(timeRange1(), event.getTimeRange());
        assertEquals(1, event.getNumberOfPersonLinked());
    }

    @Test
    public void constructor_withLinkedPersonCount_success() {
        Event event = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1(), 4);
        assertEquals(TITLE_MEETING, event.getTitle());
        assertEquals(Optional.of(DESC_A), event.getDescription());
        assertEquals(timeRange1(), event.getTimeRange());
        assertEquals(4, event.getNumberOfPersonLinked());
    }

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Event(null, Optional.empty(), timeRange1()));
        assertThrows(NullPointerException.class, () -> new Event(TITLE_MEETING, null, timeRange1()));
        assertThrows(NullPointerException.class, () -> new Event(TITLE_MEETING, Optional.empty(), null));
    }

    @Test
    public void incrementDecrement_numberOfPersonLinked_updates() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        event.incrementNumberOfPersonLinked();
        assertEquals(2, event.getNumberOfPersonLinked());
        event.decrementNumberOfPersonLinked();
        assertEquals(1, event.getNumberOfPersonLinked());
        event.decrementNumberOfPersonLinked();
        assertEquals(0, event.getNumberOfPersonLinked());
    }

    @Test
    public void decrement_belowZero_throwsIllegalStateException() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        event.decrementNumberOfPersonLinked();
        assertThrows(IllegalStateException.class, event::decrementNumberOfPersonLinked);
    }

    @Test
    public void isSameEvent_sameIdentity_returnsTrue() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_MEETING, Optional.of(DESC_B), timeRange1());
        assertTrue(eventA.isSameEvent(eventB));
    }

    @Test
    public void isSameEvent_sameObject_returnsTrue() {
        Event event = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        assertTrue(event.isSameEvent(event));
    }

    @Test
    public void isSameEvent_differentIdentity_returnsFalse() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_REVIEW, Optional.of(DESC_A), timeRange1());
        assertFalse(eventA.isSameEvent(eventB));
    }

    @Test
    public void isSameEvent_sameTitleDifferentTime_returnsFalse() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange2());
        assertFalse(eventA.isSameEvent(eventB));
    }

    @Test
    public void isSameEvent_null_returnsFalse() {
        Event event = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        assertFalse(event.isSameEvent(null));
    }

    @Test
    public void equals_sameTitleAndTimeRange_returnsTrue() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_MEETING, Optional.of(DESC_B), timeRange1());
        assertTrue(eventA.equals(eventB));
    }

    @Test
    public void equals_differentTimeRange_returnsFalse() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange2());
        assertFalse(eventA.equals(eventB));
    }

    @Test
    public void equals_differentTitle_returnsFalse() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_REVIEW, Optional.of(DESC_A), timeRange1());
        assertFalse(eventA.equals(eventB));
    }

    @Test
    public void equals_otherType_returnsFalse() {
        Event event = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        assertFalse(event.equals(5));
    }

    @Test
    public void hashCode_sameEvent_sameHash() {
        Event eventA = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        Event eventB = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        assertEquals(eventA.hashCode(), eventB.hashCode());
    }

    @Test
    public void toString_withDescription_formatsCorrectly() {
        Event event = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        String expected = String.format("Title: %s, Description: %s, Duration: From %s",
                TITLE_MEETING, DESC_A, timeRange1());
        assertEquals(expected, event.toString());
    }

    @Test
    public void toString_withoutDescription_formatsCorrectly() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        String expected = String.format("Title: %s, Duration: From %s",
                TITLE_MEETING, timeRange1());
        assertEquals(expected, event.toString());
    }

    @Test
    public void getStartAndEndTimeFormatted_returnsFormattedTimes() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        assertEquals("2026-03-25 0900", event.getStartTimeFormatted());
        assertEquals("2026-03-25 1000", event.getEndTimeFormatted());
    }

    @Test
    public void getEventId_returnsDeterministicHash() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        Event sameIdentityEvent = new Event(TITLE_MEETING, Optional.of(DESC_A), timeRange1());
        assertEquals(sameIdentityEvent.getEventId(), event.getEventId());
    }

    @Test
    public void isClashingWith_overlappingAndNonOverlapping() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        Event overlapping = new Event(TITLE_REVIEW, Optional.empty(),
                new TimeRange("2026-03-25 0930", "2026-03-25 1030"));
        Event nonOverlapping = new Event(TITLE_REVIEW, Optional.empty(),
                new TimeRange("2026-03-25 1000", "2026-03-25 1100"));
        assertTrue(event.isClashingWith(overlapping));
        assertFalse(event.isClashingWith(nonOverlapping));
    }

    @Test
    public void hasSameStartTime_sameStart_returnsTrue() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        assertTrue(event.hasSameStartTime(LocalDateTime.parse("2026-03-25T09:00")));
    }

    @Test
    public void hasSameStartTime_differentStart_returnsFalse() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        assertFalse(event.hasSameStartTime(LocalDateTime.parse("2026-03-25T10:00")));
    }

    @Test
    public void hasSameStartTime_null_throwsNullPointerException() {
        Event event = new Event(TITLE_MEETING, Optional.empty(), timeRange1());
        assertThrows(NullPointerException.class, () -> event.hasSameStartTime(null));
    }
}
