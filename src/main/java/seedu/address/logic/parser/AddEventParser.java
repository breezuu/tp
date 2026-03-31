package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TO;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@link AddEventCommand} object.
 */
public class AddEventParser implements Parser<AddEventCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddEventCommand
     * and returns an AddEventCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddEventCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_TITLE, PREFIX_DESC, PREFIX_START, PREFIX_END, PREFIX_TO,
                PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_TITLE, PREFIX_START, PREFIX_END, PREFIX_TO)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_TITLE, PREFIX_DESC, PREFIX_START, PREFIX_END, PREFIX_TO,
                PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

        Event event = createEvent(argMultimap);
        try {
            PersonInformation targetInfo = createPersonInformation(argMultimap);
            return new AddEventCommand(targetInfo, event);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE), pe);
        }
    }

    private static PersonInformation createPersonInformation(ArgumentMultimap argMultimap) throws ParseException {
        String taggedContact = argMultimap.getValue(PREFIX_TO).get().trim();
        Name name = ParserUtil.parseName(taggedContact);
        Phone phone = argMultimap.getValue(PREFIX_PHONE).isPresent()
                ? ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get())
                : null;
        Email email = argMultimap.getValue(PREFIX_EMAIL).isPresent()
                ? ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get())
                : null;
        Address address = argMultimap.getValue(PREFIX_ADDRESS).isPresent()
                ? ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get())
                : null;
        Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
        return new PersonInformation(name, phone, email, address, tags);
    }

    private static Event createEvent(ArgumentMultimap argMultimap) throws ParseException {
        String titleStr = argMultimap.getValue(PREFIX_TITLE).get().trim();
        if (!Title.isValidTitle(titleStr)) {
            throw new ParseException(Title.MESSAGE_CONSTRAINTS);
        }
        Title title = new Title(titleStr);
        String descStr = argMultimap.getValue(PREFIX_DESC).map(String::trim).orElse(null);
        if (descStr != null && !descStr.isEmpty() && !Description.isValidDescription(descStr)) {
            throw new ParseException(Description.MESSAGE_CONSTRAINTS);
        }
        Optional<Description> description = (descStr != null && !descStr.isEmpty())
                ? Optional.of(new Description(descStr))
                : Optional.empty();
        String startDateTime = argMultimap.getValue(PREFIX_START).get().trim();
        String endDateTime = argMultimap.getValue(PREFIX_END).get().trim();
        if (!TimeRange.isValidDateTimeFormat(startDateTime) || !TimeRange.isValidDateTimeFormat(endDateTime)) {
            throw new ParseException(TimeRange.MESSAGE_INVALID_DATETIME_FORMAT);
        }
        try {
            TimeRange timeRange = new TimeRange(startDateTime, endDateTime);
            return new Event(title, description, timeRange);
        } catch (IllegalArgumentException e) {
            throw new ParseException(TimeRange.MESSAGE_END_NOT_AFTER_START, e);
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
