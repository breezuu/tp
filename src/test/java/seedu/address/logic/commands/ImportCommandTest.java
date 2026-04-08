package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.ImportCommand.MESSAGE_ERROR_READING_FILE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ImportCommandTest {
    @TempDir
    public Path testFolder;

    private Model model;
    private final String personsHeader = "Name,Phone,Email,Address,Tags,EventIds,Photo,Pinned";
    private final String eventsHeader = "EventId,Title,Description,Start,End";

    private void createPersonsCsvFile(String fileName, String content) throws Exception {
        Path filePath = testFolder.resolve(fileName + "_persons.csv");
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    private void createEventsCsvFile(String fileName, String content) throws Exception {
        Path filePath = testFolder.resolve(fileName + "_events.csv");
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    public ImportCommand createTestCommand(String importType, String filename) {
        return new ImportCommand(importType, filename) {
            @Override
            protected Path getEventsImportPath(Model model) {
                return testFolder.resolve(filename + "_events.csv");
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return testFolder.resolve(filename + "_persons.csv");
            }
        };
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path personsPath = model.getAddressBookFilePath().getParent().resolve("test_import_persons.csv");
        Path eventsPath = model.getAddressBookFilePath().getParent().resolve("test_import_events.csv");
        Files.deleteIfExists(personsPath);
        Files.deleteIfExists(eventsPath);
    }

    @Test
    public void execute_overwriteImportType_wipesExistingData() throws Exception {
        Person expectedTest1 = new PersonBuilder()
                .withName("Alice")
                .withPhone("12345678")
                .withEmail("alice@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .withPhoto("hello_world.jpg")
                .build();

        Person expectedTest2 = new PersonBuilder()
                .withName("David")
                .withPhone("91234567")
                .withEmail("david@u.nus.edu")
                .withAddress("Blk 456")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        model.addPerson(expectedTest1);

        createEventsCsvFile("valid", eventsHeader);
        createPersonsCsvFile("valid", personsHeader + "\nDavid,91234567,david@u.nus.edu,Blk 456,,,,false");

        ImportCommand command = createTestCommand("overwrite", "valid");
        command.execute(model);

        assertFalse(model.hasPerson(expectedTest1));
        assertTrue(model.hasPerson(expectedTest2));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addWithDuplicates_skipsExistingData() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("12345678").build();
        model.addPerson(alice);

        String personData = "Alice Pauline,12345678,alice@u.nus.edu,Blk 123,,,,false\n"
                + "Bob,88662211,bob@u.nus.edu,Blk 123,,,,false";

        createEventsCsvFile("merge", eventsHeader);
        createPersonsCsvFile("merge", personsHeader + "\n" + personData);

        ImportCommand command = createTestCommand("add", "merge");
        command.execute(model);

        Person expectedPerson = new PersonBuilder()
                .withName("Bob")
                .withPhone("88662211")
                .withEmail("bob@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .build();

        assertEquals(9, model.getAddressBook().getPersonList().size());
        assertTrue(model.hasPerson(expectedPerson));
    }

    @Test
    public void execute_overwriteImportType_handlesEmptyOptionalField() throws Exception {
        Person expectedTest1 = new PersonBuilder()
                .withName("Alice")
                .withPhone("12345678")
                .withoutEmail()
                .withoutAddress()
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("valid", eventsHeader);
        createPersonsCsvFile("valid", personsHeader + "\nAlice,12345678,,,,,,,false");

        ImportCommand command = createTestCommand("overwrite", "valid");
        command.execute(model);

        assertTrue(model.hasPerson(expectedTest1));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_overwriteImportType_handlesPhotoField() throws Exception {
        Person expectedTest1 = new PersonBuilder()
                .withName("Alice")
                .withPhone("12345678")
                .withEmail("alice@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .withPhoto("hello_world.jpg")
                .build();

        String testPersonStr = "\nAlice,12345678,alice@u.nus.edu,Blk 123,,,hello_world.jpg,false";
        createEventsCsvFile("photo", eventsHeader);
        createPersonsCsvFile("photo", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("overwrite", "photo");
        command.execute(model);

        assertTrue(model.hasPerson(expectedTest1));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(command.execute(model).getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_importPinnedAndLegacyRows_restoresPinnedStateAndAcceptsOldFormat() throws Exception {
        Model modelWithPins = new ModelManager();

        Person pinnedPerson = new PersonBuilder()
                .withName("Pinned Alice")
                .withPhone("12345678")
                .withEmail("alice@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();
        Person legacyPerson = new PersonBuilder()
                .withName("Legacy Bob")
                .withPhone("87654321")
                .withEmail("bob@u.nus.edu")
                .withAddress("Blk 456")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("pinnedAndLegacy", eventsHeader);
        createPersonsCsvFile("pinnedAndLegacy",
                personsHeader + "\n"
                        + "Pinned Alice,12345678,alice@u.nus.edu,Blk 123,,,,true\n"
                        + "Legacy Bob,87654321,bob@u.nus.edu,Blk 456,,,,false");

        ImportCommand command = createTestCommand("overwrite", "pinnedAndLegacy");
        CommandResult result = command.execute(modelWithPins);

        assertTrue(result.getFeedbackToUser().contains("2 row(s) added"));
        assertTrue(modelWithPins.hasPerson(pinnedPerson));
        assertTrue(modelWithPins.hasPerson(legacyPerson));
        assertTrue(modelWithPins.isPersonPinned(pinnedPerson));
        assertFalse(modelWithPins.isPersonPinned(legacyPerson));
    }

    @Test
    public void execute_importPinnedFalseRow_doesNotPinPerson() throws Exception {
        Model modelWithPins = new ModelManager();

        Person person = new PersonBuilder()
                .withName("Unpinned User")
                .withPhone("92345678")
                .withEmail("unpinned@u.nus.edu")
                .withAddress("Blk 789")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("pinnedFalse", eventsHeader);
        createPersonsCsvFile("pinnedFalse",
                personsHeader + "\n"
                        + "Unpinned User,92345678,unpinned@u.nus.edu,Blk 789,,,,false");

        ImportCommand command = createTestCommand("overwrite", "pinnedFalse");
        CommandResult result = command.execute(modelWithPins);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(modelWithPins.hasPerson(person));
        assertFalse(modelWithPins.isPersonPinned(person));
    }

    @Test
    public void execute_importInvalidPinnedValue_treatsAsUnpinned() throws Exception {
        Model modelWithPins = new ModelManager();

        Person person = new PersonBuilder()
                .withName("Invalid Pin")
                .withPhone("93456789")
                .withEmail("invalidpin@u.nus.edu")
                .withAddress("Blk 111")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("invalidPinnedValue", eventsHeader);
        createPersonsCsvFile("invalidPinnedValue",
                personsHeader + "\n"
                        + "Invalid Pin,93456789,invalidpin@u.nus.edu,Blk 111,,,,yes");

        ImportCommand command = createTestCommand("overwrite", "invalidPinnedValue");
        command.execute(modelWithPins);

        assertTrue(modelWithPins.hasPerson(person));
        assertFalse(modelWithPins.isPersonPinned(person));
    }

    @Test
    public void execute_importUpperCaseTruePinnedValue_pinsPerson() throws Exception {
        Model modelWithPins = new ModelManager();

        Person person = new PersonBuilder()
                .withName("Case True")
                .withPhone("94567890")
                .withEmail("casetrue@u.nus.edu")
                .withAddress("Blk 222")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("uppercaseTrue", eventsHeader);
        createPersonsCsvFile("uppercaseTrue",
                personsHeader + "\n"
                        + "Case True,94567890,casetrue@u.nus.edu,Blk 222,,,,TRUE");

        ImportCommand command = createTestCommand("overwrite", "uppercaseTrue");
        command.execute(modelWithPins);

        assertTrue(modelWithPins.hasPerson(person));
        assertTrue(modelWithPins.isPersonPinned(person));
    }

    @Test
    public void execute_fileNotFound_throwsCommandException() {
        ImportCommand command = createTestCommand("add", "invalid_file");

        String expectedMessage = String.format(MESSAGE_ERROR_READING_FILE, "invalid_file_events.csv");
        assertCommandFailure(command, model, expectedMessage);
    }

    @Test
    public void execute_csvHeaderOnly_returnsEmptyMessage() throws Exception {
        createEventsCsvFile("empty", eventsHeader);
        createPersonsCsvFile("empty", personsHeader);

        ImportCommand command = createTestCommand("add", "empty");
        CommandResult result = command.execute(model);

        assertEquals(String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty_persons.csv"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_emptyCsvFile_throwsCommandException() throws Exception {
        Path emptyEventsPath = testFolder.resolve("empty_events.csv");
        Path emptyPersonsPath = testFolder.resolve("empty_persons.csv");
        Files.write(emptyEventsPath, new byte[0]);
        Files.write(emptyPersonsPath, new byte[0]);

        ImportCommand command = createTestCommand("add", "empty");

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty_events.csv"),
                exception.getMessage());
    }

    @Test
    public void execute_rowsWithAndWithoutPhoto_importsBothPersons() throws Exception {
        String personData = "Photo User,81234567,photo@u.nus.edu,Blk 123,,,avatar.png,false\n"
                + "No Photo,82345678,nophoto@u.nus.edu,Blk 456,,,,false";

        createEventsCsvFile("mixedPhoto", eventsHeader);
        createPersonsCsvFile("mixedPhoto", personsHeader + "\n" + personData);

        ImportCommand command = createTestCommand("overwrite", "mixedPhoto");
        command.execute(model);

        Person withPhoto = new PersonBuilder()
                .withName("Photo User")
                .withPhone("81234567")
                .withEmail("photo@u.nus.edu")
                .withAddress("Blk 123")
                .withPhoto("avatar.png")
                .build();
        Person withoutPhoto = new PersonBuilder()
                .withName("No Photo")
                .withPhone("82345678")
                .withEmail("nophoto@u.nus.edu")
                .withAddress("Blk 456")
                .withoutPhoto()
                .build();

        assertTrue(model.hasPerson(withPhoto));
        assertTrue(model.hasPerson(withoutPhoto));
    }

    @Test
    public void execute_invalidDataRow_skipsAndReports() throws Exception {
        String personData = "Valid,91234567,valid@u.nus.edu,Blk 123,,,,false\n"
                + "Invalid,abcd,invalid@u.nus.edu,Blk 123,,,,false";

        createEventsCsvFile("invalidRow", eventsHeader);
        createPersonsCsvFile("invalidRow", personsHeader + "\n" + personData);

        ImportCommand command = createTestCommand("add", "invalidRow");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(result.getFeedbackToUser().contains("1 row(s) skipped"));
    }

    @Test
    public void execute_rowWithExistingGlobalEvent_skipsDuplicateGlobalEventAdd() throws Exception {
        Event existingEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100")
        );
        model.addEvent(existingEvent);
        int eventCountBeforeImport = model.getAddressBook().getEventList().size();

        int eventId = existingEvent.getEventId();
        String testEventStr = "\n" + eventId + ",Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100";
        String testPersonStr = "\nEve,81234567,eve@u.nus.edu,Blk 321,," + eventId + ",,false";
        createEventsCsvFile("eventExists", eventsHeader + testEventStr);
        createPersonsCsvFile("eventExists", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("add", "eventExists");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertEquals(eventCountBeforeImport, model.getAddressBook().getEventList().size());
    }

    @Test
    public void execute_rowWithOverlappingGlobalEvent_skipsOverlappingGlobalEventAdd() throws Exception {
        Event existingEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100")
        );
        model.addEvent(existingEvent);
        int eventCountBeforeImport = model.getAddressBook().getEventList().size();

        createEventsCsvFile("eventOverlap", eventsHeader + "\n888,Consult,Discussion,2026-05-06 1000,2026-05-06 1100");
        createPersonsCsvFile("eventOverlap", personsHeader + "\nEve,81234567,eve@u.nus.edu,Blk 321,,888,,false");

        ImportCommand command = createTestCommand("add", "eventOverlap");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertEquals(eventCountBeforeImport, model.getAddressBook().getEventList().size());
    }


    @Test
    public void execute_fileDoesNotExist_throwsCommandException() {
        ImportCommand importCommand = new ImportCommand("add", "nonExistentFile");
        assertCommandFailure(importCommand, model,
                String.format(ImportCommand.MESSAGE_ERROR_READING_FILE, "nonExistentFile_events.csv"));
    }

    @Test
    public void execute_headerOnlyFile_returnsEmptyFileMessage() throws Exception {
        Path eventsPath = testFolder.resolve("empty_events.csv");
        Path personsPath = testFolder.resolve("empty_persons.csv");
        Files.writeString(eventsPath, eventsHeader);
        Files.writeString(personsPath, personsHeader);

        ImportCommand importCommand = new ImportCommand("add", "empty") {
            @Override
            protected Path getEventsImportPath(Model model) {
                return eventsPath;
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return personsPath;
            }
        };

        String expectedMessage = String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty_persons.csv");
        CommandResult result = importCommand.execute(model);
        assertEquals(expectedMessage, result.getFeedbackToUser());
    }

    @Test
    public void readLinesFromCsv_fileAccessError_throwsCommandException() {
        String dirname = "notAFile";
        Path dirPath = testFolder.resolve(dirname);
        assertTrue(dirPath.toFile().mkdir());

        ImportCommand importCommand = new ImportCommand("add", dirname) {
            @Override
            protected Path getEventsImportPath(Model model) {
                return dirPath;
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return dirPath.resolve(dirname + "_persons.csv");
            }
        };

        String expectedMessage = String.format(ImportCommand.MESSAGE_ERROR_READING_FILE, dirname + "_events.csv");

        assertThrows(CommandException.class, () -> importCommand.execute(model), expectedMessage);
    }

    @Test
    public void processImportedLines_invalidColumnCount_skipsRow() throws Exception {
        Path eventsPath = testFolder.resolve("malformed_events.csv");
        Path personsPath = testFolder.resolve("malformed_persons.csv");
        List<String> eventsLines = List.of(eventsHeader);
        List<String> personLines = List.of(
                personsHeader,
                "John Doe,91234567",
                "Yohan,67676868,,,"
        );
        Files.write(eventsPath, eventsLines);
        Files.write(personsPath, personLines);

        ImportCommand importCommand = new ImportCommand("add", "malformed") {
            @Override
            protected Path getEventsImportPath(Model model) {
                return eventsPath;
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return personsPath;
            }
        };

        CommandResult result = importCommand.execute(model);

        String expectedFeedback = String.format(ImportCommand.MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED,
                "malformed", 0, 2);
        assertEquals(expectedFeedback, result.getFeedbackToUser());
    }

    @Test
    public void parseEvents_validEvents_success() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        String eventString = "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|101;"
                + "Lunch||2026-05-06 1200|2026-05-06 1300|1|102";
        List<Event> result = importCommand.parseEvents(eventString, new HashMap<>());

        assertEquals(2, result.size());
        assertEquals("Meeting", result.get(0).getTitle().fullTitle);
        assertEquals("Kickoff", result.get(0).getDescription().orElseThrow().fullDescription);
        assertEquals("Lunch", result.get(1).getTitle().fullTitle);
        assertTrue(result.get(1).getDescription().isEmpty());
    }

    @Test
    public void parseEvents_nullOrEmpty_returnsEmptyList() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseEvents(null, new HashMap<>()).isEmpty());
        assertTrue(importCommand.parseEvents("", new HashMap<>()).isEmpty());
        assertTrue(importCommand.parseEvents("   ", new HashMap<>()).isEmpty());
    }

    @Test
    public void parseEvents_malformedEntries_skipsInvalidEvents() {
        String malformedString = "InvalidEvent|OnlyOnePipe; |Desc|Start|End; "
                + "ValidEvent|Note|2026-05-06 1000|2026-05-06 1100|3|888";
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        List<Event> result = importCommand.parseEvents(malformedString, new HashMap<>());

        assertEquals(1, result.size());
        assertEquals("ValidEvent", result.get(0).getTitle().fullTitle);
    }

    @Test
    public void parseEvents_invalidColumnCount_skipsMalformedEntryKeepsValidEntry() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "Invalid|Only|Four|Columns;"
                + "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|1|100";

        List<Event> result = importCommand.parseEvents(eventString, eventMap);

        assertEquals(1, result.size());
        assertEquals("Meeting", result.get(0).getTitle().fullTitle);
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsKey(100));
    }

    @Test
    public void parseEvents_missingRequiredFields_skipsEntriesWithBlankTitleStartEndOrEventId() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "|Desc|2026-05-06 1000|2026-05-06 1100|1|100;"
                + "Meeting|Desc||2026-05-06 1100|1|101;"
                + "Meeting|Desc|2026-05-06 1000||1|102;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|1|;"
                + "Valid|Desc|2026-05-06 1200|2026-05-06 1300|1|106";

        List<Event> result = importCommand.parseEvents(eventString, eventMap);

        assertEquals(1, result.size());
        assertEquals("Valid", result.get(0).getTitle().fullTitle);
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsKey(106));
    }

    @Test
    public void parseEvents_nonPositiveLinkedCount_skipsZeroAndNegativeCountsKeepsPositiveCount() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|-1|104;"
                + "Valid|Desc|2026-05-06 1200|2026-05-06 1300|1|105";

        List<Event> result = importCommand.parseEvents(eventString, eventMap);

        assertEquals(1, result.size());
        assertEquals("Valid", result.get(0).getTitle().fullTitle);
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsKey(105));
    }

    @Test
    public void parseTags_validTagString_returnsTagSet() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        var tags = importCommand.parseTags("friends;colleagues");
        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("friends")));
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("colleagues")));
    }

    @Test
    public void parseTags_nullOrEmpty_returnsEmptySet() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseTags(null).isEmpty());
        assertTrue(importCommand.parseTags("").isEmpty());
        assertTrue(importCommand.parseTags("   ").isEmpty());
    }

    @Test
    public void parseTags_withEmptyTokens_ignoresBlankTags() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        var tags = importCommand.parseTags("friends;; colleagues; ;");

        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("friends")));
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("colleagues")));
    }

    @Test
    public void parseTags_onlyBlankTokens_returnsEmptySet() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseTags(" ; ; ").isEmpty());
    }

    @Test
    public void execute_rowWithTags_importsPerson() throws Exception {
        String testPersonStr = "\nCharlie,87654321,charlie@u.nus.edu,Blk 789,friends;CS2103,,,false";
        createEventsCsvFile("tagged", eventsHeader);
        createPersonsCsvFile("tagged", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("add", "tagged");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_rowWithEvents_importsPerson() throws Exception {
        int eventId = 500;
        String testEventStr = eventId + ",Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100";
        String testPersonStr = "\nEve,81234567,eve@u.nus.edu,Blk 321,," + eventId + ",,false";
        createEventsCsvFile("withevents", eventsHeader + "\n" + testEventStr);
        createPersonsCsvFile("withevents", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("add", "withevents");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_eventsCsvContainsBlankLine_skipsBlankEventEntryAndImportsPerson() throws Exception {
        createEventsCsvFile("blankEventLine", eventsHeader + "\n   ");
        createPersonsCsvFile("blankEventLine",
                personsHeader + "\nBlank Event User,81231234,blank@u.nus.edu,Blk 10,,,,false");

        ImportCommand command = createTestCommand("overwrite", "blankEventLine");
        CommandResult result = command.execute(model);

        Person expectedPerson = new PersonBuilder()
                .withName("Blank Event User")
                .withPhone("81231234")
                .withEmail("blank@u.nus.edu")
                .withAddress("Blk 10")
                .withoutPhoto()
                .build();

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(model.hasPerson(expectedPerson));
        assertEquals(0, model.getAddressBook().getEventList().size());
    }

    @Test
    public void execute_eventsCsvMalformedRow_triggersCatchAndContinuesImportingPersons() throws Exception {
        // Missing required event columns causes IllegalArgumentException in event parsing.
        createEventsCsvFile("malformedEventRow", eventsHeader + "\n999,OnlyTitle");
        createPersonsCsvFile("malformedEventRow",
                personsHeader + "\nMalformed Event User,81239999,malformed@u.nus.edu,Blk 11,,,,false");

        ImportCommand command = createTestCommand("overwrite", "malformedEventRow");
        CommandResult result = command.execute(model);

        Person expectedPerson = new PersonBuilder()
                .withName("Malformed Event User")
                .withPhone("81239999")
                .withEmail("malformed@u.nus.edu")
                .withAddress("Blk 11")
                .withoutPhoto()
                .build();

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(model.hasPerson(expectedPerson));
        assertEquals(0, model.getAddressBook().getEventList().size());
    }

    @Test
    public void parseEvents_sameEventId_reusesSharedEventInstance() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        List<Event> firstParsed = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);
        List<Event> secondParsed = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);

        assertEquals(1, firstParsed.size());
        assertEquals(1, secondParsed.size());
        assertSame(firstParsed.get(0), secondParsed.get(0));
    }

    @Test
    public void parseEvents_sameEventIdDifferentTitleOrTime_skipsConflictingEvent() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        List<Event> firstParsed = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);
        List<Event> conflictingTitle = importCommand.parseEvents(
                "Consultation|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);
        List<Event> conflictingTime = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1200|2026-05-06 1300|2|900", eventMap);

        assertEquals(1, firstParsed.size());
        assertTrue(conflictingTitle.isEmpty());
        assertTrue(conflictingTime.isEmpty());
        assertEquals("Meeting", eventMap.get(900).getTitle().fullTitle);
    }

    @Test
    public void parseEvents_malformedAndIncompleteEntries_areSkipped() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String malformedString = "|Desc|2026-05-06 1000|2026-05-06 1100|1|100;"
                + "Meeting|Desc||2026-05-06 1100|1|101;"
                + "Meeting|Desc|2026-05-06 1000||1|102;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|1|";

        List<Event> result = importCommand.parseEvents(malformedString, eventMap);

        assertTrue(result.isEmpty());
        assertTrue(eventMap.isEmpty());
    }

    @Test
    public void parseEvents_invalidFieldValues_areSkipped() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String invalidString = "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|-1|104;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|abc|105";

        List<Event> result = importCommand.parseEvents(invalidString, eventMap);

        assertTrue(result.isEmpty());
        assertTrue(eventMap.isEmpty());
    }

    @Test
    public void parseEvents_requiredFieldsLinkedCountAndException_skipsInvalidKeepsValid() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "|Desc|2026-05-06 1000|2026-05-06 1100|1|100;"
                + "Meeting|Desc||2026-05-06 1100|1|101;"
                + "Meeting|Desc|2026-05-06 1000||1|102;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|1|;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|-1|104;"
                + "Broken|Desc|not-a-time|2026-05-06 1100|1|105;"
                + "Valid|Desc|2026-05-06 1200|2026-05-06 1300|1|106";

        List<Event> result = importCommand.parseEvents(eventString, eventMap);

        assertEquals(1, result.size());
        assertEquals("Valid", result.get(0).getTitle().fullTitle);
        assertEquals(1, eventMap.size());
    }

    @Test
    public void parseEvents_multipleEventsWithoutConflict_importsAllEvents() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900;"
                + "Lunch||2026-05-06 1200|2026-05-06 1300|1|901;"
                + "Tutorial|CS2103|2026-05-06 1400|2026-05-06 1500|1|902";

        List<Event> parsedEvents = importCommand.parseEvents(eventString, eventMap);

        assertEquals(3, parsedEvents.size());
        assertEquals("Meeting", parsedEvents.get(0).getTitle().fullTitle);
        assertEquals("Lunch", parsedEvents.get(1).getTitle().fullTitle);
        assertEquals("Tutorial", parsedEvents.get(2).getTitle().fullTitle);
        assertEquals(3, eventMap.size());
    }

    @Test
    public void parseEvents_multipleEventsWithConflict_skipsOnlyConflictingEvent() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900;"
                + "Consultation|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900;"
                + "Tutorial|CS2103|2026-05-06 1400|2026-05-06 1500|1|901";

        List<Event> parsedEvents = importCommand.parseEvents(eventString, eventMap);

        assertEquals(2, parsedEvents.size());
        assertEquals("Meeting", parsedEvents.get(0).getTitle().fullTitle);
        assertEquals("Tutorial", parsedEvents.get(1).getTitle().fullTitle);
        assertEquals(2, eventMap.size());
        assertEquals("Meeting", eventMap.get(900).getTitle().fullTitle);
    }

    @Test
    public void parseLineToEvent_nullLine_returnsEmptyOptional() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseLineToEvent(null).isEmpty());
    }

    @Test
    public void parseLineToEvent_blankLine_returnsEmptyOptional() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseLineToEvent("   ").isEmpty());
    }

    @Test
    public void createEventFromCsvRow_missingRequiredFields_throwsIllegalArgumentException() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("101,,desc,2026-05-06 1000,2026-05-06 1100"));
        assertEquals("Event has missing required fields", exception.getMessage());
    }

    @Test
    public void createEventFromCsvRow_missingStartOrEnd_throwsIllegalArgumentException() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        IllegalArgumentException missingStart = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("101,Meeting,desc,,2026-05-06 1100"));
        assertEquals("Event has missing required fields", missingStart.getMessage());

        IllegalArgumentException missingEnd = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("101,Meeting,desc,2026-05-06 1000,"));
        assertEquals("Event has missing required fields", missingEnd.getMessage());
    }

    @Test
    public void createEventFromCsvRow_nonNumericEventId_throwsIllegalArgumentException() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("abc,Meeting,desc,2026-05-06 1000,2026-05-06 1100"));
        assertEquals("Event ID must be a valid integer", exception.getMessage());
    }

    @Test
    public void parseEventIds_nullOrBlank_returnsEmptyList() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        assertTrue(importCommand.parseEventIds(null, new HashMap<>()).isEmpty());
        assertTrue(importCommand.parseEventIds("   ", new HashMap<>()).isEmpty());
    }

    @Test
    public void parseEventIds_existingAndInvalidIds_handlesAllBranches() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        Event existingEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100")
        );
        eventMap.put(100, existingEvent);

        List<Event> result = importCommand.parseEventIds("100;999;abc", eventMap);

        // Inside the parseEventIds() method,
        // event ID '100' covers the 'event != null' branch, event ID '999' covers the 'else' branch,
        // and event ID 'abc' covers the NumberFormatException branch.
        assertEquals(1, result.size());
        assertSame(existingEvent, result.get(0));
    }

    @Test
    public void equals() {
        ImportCommand importFirst = new ImportCommand("overwrite", "file1");
        ImportCommand importSecond = new ImportCommand("overwrite", "file1");
        ImportCommand importDifferentFile = new ImportCommand("overwrite", "file2");
        ImportCommand importDifferentType = new ImportCommand("add", "file1");
        ImportCommand importDifferentTypeAndFile = new ImportCommand("add", "file3");

        assertEquals(importFirst, importFirst);
        assertEquals(importFirst, importSecond);
        assertFalse(importFirst.equals(importDifferentType));
        assertFalse(importFirst.equals(importDifferentFile));
        assertFalse(importFirst.equals(importDifferentTypeAndFile));
        assertNotEquals(null, importFirst);
        assertNotEquals(importFirst, new ExportCommand("all", "test_file"));

    }

    @Test
    public void toString_returnsExpectedString() {
        ImportCommand importCommand = new ImportCommand("add", "myFile");
        assertEquals("Importing list from: myFile", importCommand.toString());
    }
}
