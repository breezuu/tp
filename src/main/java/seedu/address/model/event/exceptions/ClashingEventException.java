package seedu.address.model.event.exceptions;

/**
 * Signals that the operation will result in duplicate Events.
 */
public class ClashingEventException extends RuntimeException {
    public ClashingEventException() {
        super("The event clashes with 1 or more existing events.");
    }
}
