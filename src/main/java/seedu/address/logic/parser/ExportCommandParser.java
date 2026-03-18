package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.stream.Stream;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class ExportCommandParser implements Parser<ExportCommand> {

    public static final Prefix PREFIX_TYPE = new Prefix("t/");
    public static final Prefix PREFIX_FILENAME = new Prefix("f/");

    public static final String MESSAGE_INVALID_EXPORT_TYPE = "Invalid export type! Use 'all' or 'current'.";
    public static final String MESSAGE_EMPTY_FILENAME = "Filename cannot be empty!";
    public static final String MESSAGE_INVALID_FILENAME = "Invalid filename! Use alphanumeric characters. ";

    public ExportCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TYPE, PREFIX_FILENAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_TYPE, PREFIX_FILENAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TYPE, PREFIX_FILENAME);

        String exportType = argMultimap.getValue(PREFIX_TYPE).get().trim();
        if (!exportType.equalsIgnoreCase("all") && !exportType.equalsIgnoreCase("current")) {
            throw new ParseException(String.format(MESSAGE_INVALID_EXPORT_TYPE, ExportCommand.MESSAGE_USAGE));
        }

        String filename = argMultimap.getValue(PREFIX_FILENAME).get().trim();
        if (filename.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_EMPTY_FILENAME, ExportCommand.MESSAGE_USAGE));
        }

        if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
            throw new ParseException(String.format(MESSAGE_INVALID_FILENAME, ExportCommand.MESSAGE_USAGE));
        }

        return new ExportCommand(exportType, filename);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
