package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;

/**
 * A UI component that displays information of an {@code Event}.
 */
public class EventCard extends UiPart<Region> {

    private static final String FXML = "EventListCard.fxml";

    public final Event event;

    @FXML
    private HBox cardPane;
    @FXML
    private Label title;
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
        title.setText("Title (/title): " + event.getTitle());
        title.setStyle("-fx-font-weight: bold;");

        String descText = event.getDescription()
                .map(Description::toString)
                .map(d -> "Description (/desc): " + d)
                .orElse("");
        description.setText(descText);
        description.setStyle("-fx-font-style: italic;");
        boolean hasDescription = event.getDescription().isPresent();
        description.setVisible(hasDescription);
        description.setManaged(hasDescription);
        from.setText("Start (/start): " + event.getStartTimeFormatted());
        to.setText("End (/end): " + event.getEndTimeFormatted());
    }
}
