package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CsvUtil.sanitizeAndWrapValue;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_FILENAME;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_TYPE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;

public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    private static final String FILENAME_SUFFIX = ".csv";
    private static final String NAME_COLUMN_HEADER = "Name";
    private static final String PHONE_COLUMN_HEADER = "Phone";
    private static final String EMAIL_COLUMN_HEADER = "Email";
    private static final String ADDRESS_COLUMN_HEADER = "Address";
    private static final String TAG_COLUMN_HEADER = "Tags";
    private static final String EVENT_COLUMN_HEADER = "Events";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Exports a list of contacts into a CSV formatted file for future use.\n"
            + "Parameters: "
            + PREFIX_TYPE + "EXPORT TYPE "
            + PREFIX_FILENAME + "FILENAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TYPE + "all "
            + PREFIX_FILENAME + "myContacts";

    public static final String MESSAGE_SUCCESS = "Exported list to %1$s";

    public static final String MESSAGE_FAILURE = "Failed to export list to %1$s";

    private final String exportType;
    private final String filename;

    public ExportCommand(String exportType, String filename) {
        this.exportType = exportType;
        this.filename = filename;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> exportedList = getExportedList(model);

        Path exportPath = getExportPath(model);

        exportDataToCsv(exportPath, exportedList);

        return new CommandResult(String.format(MESSAGE_SUCCESS,
                filename + FILENAME_SUFFIX));
    }

    private void exportDataToCsv(Path exportPath, List<Person> exportedList) throws CommandException {
        try {
            FileUtil.createParentDirsOfFile(exportPath);

            String csvData = convertToCsv(exportedList);

            FileUtil.writeToFile(exportPath, csvData);
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_FAILURE, filename + FILENAME_SUFFIX));
        }
    }

    protected Path getExportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + FILENAME_SUFFIX);
    }

    private List<Person> getExportedList(Model model) {
        return exportType.equalsIgnoreCase("all")
                ? model.getAddressBook().getPersonList()
                : model.getFilteredPersonList();
    }

    private String convertToCsv(List<Person> exportedList) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append(NAME_COLUMN_HEADER).append(",")
                .append(PHONE_COLUMN_HEADER).append(",")
                .append(EMAIL_COLUMN_HEADER).append(",")
                .append(ADDRESS_COLUMN_HEADER).append(",")
                .append(TAG_COLUMN_HEADER).append(",")
                .append(EVENT_COLUMN_HEADER).append("\n");

        for (Person p : exportedList) {
            csvBuilder.append(formatPersonToRow(p)).append("\n");
        }

        return csvBuilder.toString();
    }

    private String formatPersonToRow(Person p) {
        return String.format("%s,%s,%s,%s,%s,%s",
                p.getName().fullName,
                p.getPhone().value,
                sanitizeAndWrapValue(getEmailValue(p)),
                sanitizeAndWrapValue(getAddressValue(p)),
                sanitizeAndWrapValue(formatTags(p)),
                sanitizeAndWrapValue(formatEvents(p))
        );
    }

    private String getEmailValue(Person p) {
        return p.getEmail().map(e -> e.value).orElse("");
    }

    private String getAddressValue(Person p) {
        return p.getAddress().map(a -> a.value).orElse("");
    }

    private String formatTags(Person p) {
        return p.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private String formatSingleEvent(Event e) {
        String sanitizedDesc = e.getDescription().replace(";", "-");

        return String.format("%s|%s|%s",
                sanitizedDesc,
                e.getStartTime(),
                e.getEndTime());
    }

    private String formatEvents(Person p) {
        return p.getEvents().stream()
                .map(this::formatSingleEvent)
                .collect(Collectors.joining(";"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ExportCommand)) {
            return false;
        }

        ExportCommand otherExportCommand = (ExportCommand) other;
        return exportType.equals(otherExportCommand.exportType)
                && filename.equals(otherExportCommand.filename);
    }

    @Override
    public String toString() {
        return String.format("Exporting list to: %s", filename + FILENAME_SUFFIX);
    }
}
