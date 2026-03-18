package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindEventParserTest {

    private final FindEventParser parser = new FindEventParser();

    @Test
    public void parse_missingNamePrefix_failure() {
        assertParseFailure(parser, " Amy Bee",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateNamePrefix_failure() {
        assertParseFailure(parser, " n/Amy Bee n/Bob Choo",
                MESSAGE_DUPLICATE_FIELDS + PREFIX_NAME);
    }

    @Test
    public void parse_validArgs_returnsFindEventCommand() {
        FindEventCommand expectedCommand =
                new FindEventCommand(new NameContainsKeywordsPredicate(Arrays.asList("Amy", "Bee")));

        assertParseSuccess(parser, " n/Amy Bee", expectedCommand);
    }
}
