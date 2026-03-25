package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TitleTest {

    @Test
    public void constructor_validTitle_success() {
        Title title = new Title("Project Update 1");
        assertEquals("Project Update 1", title.fullTitle);
    }

    @Test
    public void constructor_invalidTitle_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Title(""));
        assertThrows(IllegalArgumentException.class, () -> new Title("  Leading"));
        assertThrows(IllegalArgumentException.class, () -> new Title("Trailing "));
        assertThrows(IllegalArgumentException.class, () -> new Title("Two  Spaces"));
        assertThrows(IllegalArgumentException.class, () -> new Title("Invalid!"));
    }

    @Test
    public void isValidTitle() {
        assertTrue(Title.isValidTitle("Alpha 1"));
        assertFalse(Title.isValidTitle("Bad#Title"));
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        assertTrue(new Title("Meeting").equals(new Title("Meeting")));
    }

    @Test
    public void equals_differentValue_returnsFalse() {
        assertFalse(new Title("Meeting").equals(new Title("Review")));
    }

    @Test
    public void equals_otherType_returnsFalse() {
        assertFalse(new Title("Meeting").equals(5));
    }

    @Test
    public void hashCode_sameValue_sameHash() {
        assertEquals(new Title("Meeting").hashCode(), new Title("Meeting").hashCode());
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Meeting", new Title("Meeting").toString());
    }
}
