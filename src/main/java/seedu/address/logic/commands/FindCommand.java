package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;



/**
 * Finds and lists persons in the address book that match the given contact information.
 * Name is required, while phone, email, address, and tags are optional refinements.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds contacts by name, with optional refinements.\n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "Alex Tan "
            + PREFIX_PHONE + "91234567";

    private final PersonInformation targetInfo;

    /**
     * Creates a {@code FindCommand} using the provided matching criteria.
     */
    public FindCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
    }

    /**
     * Applies the matching criteria and updates the filtered person list.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        List<Person> matches = model.findPersons(targetInfo);
        Set<Person> matchingPersons = Set.copyOf(matches);
        Predicate<Person> showMatchingPersons = matchingPersons::contains;
        model.updateFilteredPersonList(showMatchingPersons);

        int count = model.getFilteredPersonList().size();
        if (count == 1) {
            Person matchedPerson = model.getFilteredPersonList().get(0);
            model.updateFilteredEventList(event -> matchedPerson.getEvents().contains(event));
            return new CommandResult(Messages.MESSAGE_ONE_PERSON_LISTED_OVERVIEW);
        }

        // No events in the panel if there is no confirmed match.
        model.updateFilteredEventList(event -> false);

        if (count == 0) {
            return new CommandResult(Messages.MESSAGE_NO_PERSONS);
        }
        return new CommandResult(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, count));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FindCommand otherFindCommand) {
            return targetInfo.equals(otherFindCommand.targetInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetName", targetInfo.name)
                .toString();
    }
}
