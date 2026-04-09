package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.model.tag.Tag;

public class FilterCommandParserTest {

    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_validInputWithEmptyEntries_success() {
        assertParseSuccess(parser, " " + PREFIX_TAG + "friends, , family ,",
                new FilterCommand(new TagContainsKeywordsPredicate(List.of("friends", "family"))));
    }

    @Test
    public void parse_multipleTagPrefixes_failure() {
        assertParseFailure(parser, " " + PREFIX_TAG + "friends " + PREFIX_TAG + "family",
                "Please provide all tags after a single 't/' prefix, separated by commas.");
    }

    @Test
    public void parse_missingTagPrefix_failure() {
        assertParseFailure(parser, " friends",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        "Missing required tag prefix 't/'.\n" + FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, "oops " + PREFIX_TAG + "friends",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_blankTagValue_failure() {
        assertParseFailure(parser, " " + PREFIX_TAG + "   ",
                "Error: Tag value cannot be empty.\n"
                        + "Fix: Provide a valid tag name after 't/' (e.g. filter t/CS2103 Group)");
    }

    @Test
    public void parse_invalidTagValue_failure() {
        assertParseFailure(parser, " " + PREFIX_TAG + "friends, invalid!",
                Tag.MESSAGE_CONSTRAINTS);
    }
}
