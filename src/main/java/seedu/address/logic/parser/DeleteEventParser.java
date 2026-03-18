package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@link DeleteEventCommand} object.
 */
public class DeleteEventParser implements Parser<DeleteEventCommand> {

    // Event-specific prefixes defined locally to avoid conflict with CliSyntax.PREFIX_EMAIL (e/)
    private static final Prefix PREFIX_START = new Prefix("s/");
    private static final Prefix PREFIX_END = new Prefix("e/");

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteEventCommand
     * and returns a DeleteEventCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public DeleteEventCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE,
                        PREFIX_ADDRESS, PREFIX_TAG, PREFIX_START, PREFIX_END);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_START, PREFIX_END)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE,
                PREFIX_ADDRESS, PREFIX_START, PREFIX_END);

        try {
            // Manually construct PersonInformation — cannot use PersonInformationParser here
            // because e/ is reserved for event end datetime, not email
            // TODO: Fix Prefix conflict
            Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
            Phone phone = argMultimap.getValue(PREFIX_PHONE).isPresent()
                    ? ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get())
                    : null;
            Address address = argMultimap.getValue(PREFIX_ADDRESS).isPresent()
                    ? ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get())
                    : null;
            Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
            PersonInformation targetInfo = new PersonInformation(name, phone, null, address, tags);

            // Parse event-specific fields
            String startTime = argMultimap.getValue(PREFIX_START).get().trim();
            String endTime = argMultimap.getValue(PREFIX_END).get().trim();

            return new DeleteEventCommand(targetInfo, startTime, endTime);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            DeleteEventCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
