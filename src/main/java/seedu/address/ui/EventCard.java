package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Event;

/**
 * A UI component that displays information of an {@code Event}.
 */
public class EventCard extends UiPart<Region> {

    private static final String FXML = "EventListCard.fxml";

    public final Event event;

    @FXML
    private HBox cardPane;
    @FXML
    private Label description;
    @FXML
    private Label from;
    @FXML
    private Label to;

    /**
     * Creates an {@code EventCard} with the given {@code Event}.
     */
    public EventCard(Event event) {
        super(FXML);
        this.event = event;
        description.setText(event.getDescription());
        from.setText("Start: " + event.getStartTime());
        to.setText("End: " + event.getEndTime());
    }
}
