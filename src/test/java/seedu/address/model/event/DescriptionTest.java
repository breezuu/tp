package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class DescriptionTest {

    @Test
    public void constructor_validDescription_success() {
        Description description = new Description("Discuss milestones");
        assertEquals("Discuss milestones", description.fullDescription);
    }

    @Test
    public void constructor_invalidDescription_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Description(""));
        assertThrows(IllegalArgumentException.class, () -> new Description("  Leading"));
        assertThrows(IllegalArgumentException.class, () -> new Description("Trailing "));
        assertThrows(IllegalArgumentException.class, () -> new Description("Two  Spaces"));
        assertThrows(IllegalArgumentException.class, () -> new Description("Invalid!"));
    }

    @Test
    public void isValidDescription() {
        assertTrue(Description.isValidDescription("Alpha 1"));
        assertFalse(Description.isValidDescription("Bad#Description"));
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        assertTrue(new Description("Notes").equals(new Description("Notes")));
    }

    @Test
    public void equals_differentValue_returnsFalse() {
        assertFalse(new Description("Notes").equals(new Description("Other")));
    }

    @Test
    public void equals_otherType_returnsFalse() {
        assertFalse(new Description("Notes").equals(5));
    }

    @Test
    public void hashCode_sameValue_sameHash() {
        assertEquals(new Description("Notes").hashCode(), new Description("Notes").hashCode());
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Notes", new Description("Notes").toString());
    }
}
