package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Finds and displays all events associated with persons whose names contain any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class FindEventCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Finds all events from persons whose names contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " yikleong john";

    private final NameContainsKeywordsPredicate predicate;

    /**
     * Creates a FindEventCommand to find all events from persons whose names contain the keywords.
     *
     * @param predicate the predicate to filter persons by name
     */
    public FindEventCommand(NameContainsKeywordsPredicate predicate) {
        requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        // Collect all events from persons that match the predicate
        List<Event> matchingEvents = new ArrayList<>();
        for (Person person : model.getAddressBook().getPersonList()) {
            if (predicate.test(person)) {
                matchingEvents.addAll(person.getEvents());
            }
        }

        // Create a predicate that matches only the collected events
        model.updateFilteredEventList(event -> matchingEvents.contains(event));

        return new CommandResult(
                String.format(Messages.MESSAGE_EVENTS_LISTED_OVERVIEW, model.getFilteredEventList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FindEventCommand)) {
            return false;
        }

        FindEventCommand otherCommand = (FindEventCommand) other;
        return predicate.equals(otherCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
