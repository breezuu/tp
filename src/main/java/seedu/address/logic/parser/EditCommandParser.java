package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHOTO;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {
    private static final String SEGMENT_DELIMITER = " -- ";

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        // Enforce exactly one space on both sides of "--".
        int delimiterIndex = trimmedArgs.indexOf(SEGMENT_DELIMITER);
        if (delimiterIndex < 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        String targetSegment = trimmedArgs.substring(0, delimiterIndex);
        String updateSegment = trimmedArgs.substring(delimiterIndex + SEGMENT_DELIMITER.length());

        // Reject additional spaces adjacent to the delimiter.
        if (targetSegment.endsWith(" ") || updateSegment.startsWith(" ")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        targetSegment = targetSegment.trim();
        updateSegment = updateSegment.trim();
        if (targetSegment.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        ArgumentMultimap targetMultimap =
            ArgumentTokenizer.tokenize(" " + targetSegment, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_TAG);
        targetMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

        if (!targetMultimap.getPreamble().trim().isEmpty() || targetMultimap.getValue(PREFIX_NAME).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        PersonInformation targetInfo;
        // Parse the target person's information from the target segment, ensuring that the name is present and valid
        try {
            Name targetName = ParserUtil.parseName(targetMultimap.getValue(PREFIX_NAME).get());
            Phone targetPhone = targetMultimap.getValue(PREFIX_PHONE).isPresent()
                ? ParserUtil.parsePhone(targetMultimap.getValue(PREFIX_PHONE).get())
                : null;
            Email targetEmail = targetMultimap.getValue(PREFIX_EMAIL).isPresent()
                ? ParserUtil.parseEmail(targetMultimap.getValue(PREFIX_EMAIL).get())
                : null;
            Address targetAddress = targetMultimap.getValue(PREFIX_ADDRESS).isPresent()
                ? ParserUtil.parseAddress(targetMultimap.getValue(PREFIX_ADDRESS).get())
                : null;
            Set<Tag> targetTags = ParserUtil.parseTags(targetMultimap.getAllValues(PREFIX_TAG));
            targetInfo = new PersonInformation(targetName, targetPhone, targetEmail, targetAddress, targetTags);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), pe);
        }

        // Parse the updated information from the update segment,
        // ensuring that at least one field is provided for editing
        ArgumentMultimap updateMultimap =
            ArgumentTokenizer.tokenize(" " + updateSegment, PREFIX_NAME, PREFIX_PHONE,
            PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_TAG, PREFIX_PHOTO);

        updateMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_PHOTO);

        if (!updateMultimap.getPreamble().trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (updateMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(updateMultimap.getValue(PREFIX_NAME).get()));
        }
        if (updateMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(updateMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (updateMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(updateMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (updateMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(updateMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        if (updateMultimap.getValue(PREFIX_PHOTO).isPresent()) {
            editPersonDescriptor.setPhoto(ParserUtil.parsePhoto(updateMultimap.getValue(PREFIX_PHOTO).get()));
        }

        parseTagsForEdit(updateMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(targetInfo, editPersonDescriptor);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contain only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }

        boolean allBlank = tags.stream().allMatch(String::isBlank);
        if (allBlank) {
            return Optional.of(Collections.emptySet());
        }

        return Optional.of(ParserUtil.parseTags(tags));
    }

}
