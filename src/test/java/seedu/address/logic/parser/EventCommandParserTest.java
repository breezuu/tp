package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.commands.FindEventCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;

public class EventCommandParserTest {

    private final EventCommandParser parser = new EventCommandParser();

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "   ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownSubcommand_failure() {
        assertParseFailure(parser, "xyz",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_addSubcommandNoArgs_failure() {
        assertParseFailure(parser, "add",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_viewSubcommandNoArgs_failure() {
        assertParseFailure(parser, "view",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_addSubcommand_success() {
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")),
                new TimeRange("2026-02-21 1100", "2026-02-21 1500"));
        PersonInformation expectedInfo = new PersonInformation(new Name("Amy Bee"), null, null, null, null);
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        String userInput = "add title/Complete feature list desc/All tasks start/2026-02-21 1100 "
                + "end/2026-02-21 1500 "
                + "to/Amy Bee";

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_deleteSubcommand_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name("Amy Bee"), null, null, null, null);
        Event expectedEvent = new Event(new Title("Complete feature list"), Optional.empty(),
                new TimeRange("2026-02-21 1100", "2026-02-21 1500"));
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser, "delete title/Complete feature list start/2026-02-21 1100 "
                + "end/2026-02-21 1500 n/Amy Bee", expectedCommand);
    }

    @Test
    public void parse_viewSubcommand_success() {
        FindEventCommand expectedCommand =
                new FindEventCommand(new PersonInformation(new Name("Amy Bee"), null, null, null, null));

        assertParseSuccess(parser, "view n/Amy Bee", expectedCommand);
    }
}
