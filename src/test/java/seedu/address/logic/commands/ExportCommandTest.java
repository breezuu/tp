package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ExportCommandTest {
    private Model model;
    private Model expectedModel;
    private final String testFileName = "test_export";

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path path = model.getAddressBookFilePath().getParent().resolve(testFileName + ".csv");
        Files.deleteIfExists(path);
    }

    @Test
    public void execute_exportAll_success() {
        ExportCommand exportCommand = new ExportCommand("all", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName + ".csv");

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);

        Path path = model.getAddressBookFilePath().getParent().resolve(testFileName + ".csv");
        assertTrue(Files.exists(path));
    }

    @Test
    public void execute_exportCurrent_success() {
        model.updateFilteredPersonList(p -> p.getName().fullName.contains("Alice"));
        expectedModel.updateFilteredPersonList(p -> p.getName().fullName.contains("Alice"));

        ExportCommand exportCommand = new ExportCommand("current", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName + ".csv");

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_csvContentIntegrity_success() throws Exception {
        ExportCommand command = new ExportCommand("all", testFileName);
        command.execute(model);

        Path path = model.getAddressBookFilePath().getParent().resolve(testFileName + ".csv");
        List<String> lines = Files.readAllLines(path);

        String header = lines.get(0);
        assertEquals("Name,Phone,Email,Address,Tags,Events", header);
        assertEquals(5, countCommas(header), "Header should have exactly 5 commas");

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            assertEquals(5, countCommas(line), "Row " + i + " has shifted columns!");
        }
    }

    private long countCommas(String s) {
        return s.chars().filter(ch -> ch == ',').count();
    }

    @Test
    public void execute_ioException_throwsCommandException() throws IOException {
        ExportCommand commandWithInvalidPath = new ExportCommand("all", "testFile") {
            @Override
            protected Path getExportPath(Model model) {
                return Path.of("invalid\0path");
            }
        };

        String expectedMessage = String.format(ExportCommand.MESSAGE_FAILURE, "testFile.csv");
        assertCommandFailure(commandWithInvalidPath, model, expectedMessage);
    }

    @Test
    public void equals() {
        ExportCommand exportFirst = new ExportCommand("all", "file1");
        ExportCommand exportSecond = new ExportCommand("all", "file1");

        assertTrue(exportFirst.equals(exportFirst));

        assertTrue(exportFirst.equals(exportSecond));

        assertTrue(!exportFirst.equals(null));
    }
}