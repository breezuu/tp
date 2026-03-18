package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;

public class DeleteEventParserTest {

    private static final String VALID_NAME = "Alex Yeoh";
    private static final String VALID_START = "21-02-26 1100";
    private static final String VALID_END = "21-02-26 1500";

    private final DeleteEventParser parser = new DeleteEventParser();

    @Test
    public void parse_allFieldsPresent_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        DeleteEventCommand expectedCommand =
                new DeleteEventCommand(expectedInfo, VALID_START, VALID_END);

        assertParseSuccess(parser,
                " n/" + VALID_NAME + " start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_missingNamePrefix_failure() {
        assertParseFailure(parser,
                " start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingStartPrefix_failure() {
        assertParseFailure(parser,
                " n/" + VALID_NAME + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingEndPrefix_failure() {
        assertParseFailure(parser,
                " n/" + VALID_NAME + " start/" + VALID_START,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateNamePrefix_failure() {
        assertParseFailure(parser,
                " n/" + VALID_NAME + " n/Bob start/" + VALID_START + " end/" + VALID_END,
                MESSAGE_DUPLICATE_FIELDS + "n/");
    }

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "  ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_withOptionalPhone_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), new Phone("91234567"), null, null, null);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, VALID_START, VALID_END);

        assertParseSuccess(parser,
                " n/" + VALID_NAME + " p/91234567 start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalAddress_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, null, new Address("Blk 123 Clementi Ave"), null);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, VALID_START, VALID_END);

        assertParseSuccess(parser,
                " n/" + VALID_NAME + " a/Blk 123 Clementi Ave start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalEmail_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, new Email("alex@example.com"), null, null);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, VALID_START, VALID_END);

        assertParseSuccess(parser,
                " n/" + VALID_NAME + " e/alex@example.com start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_invalidPhone_failure() {
        assertParseFailure(parser,
                " n/" + VALID_NAME + " p/notaphone start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidEmail_failure() {
        assertParseFailure(parser,
                " n/" + VALID_NAME + " e/not-an-email start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }
}
