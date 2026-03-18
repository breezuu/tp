package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.testutil.PersonBuilder;

public class DeleteEventCommandTest {

    private static final String VALID_NAME = "Amy Bee";
    private static final String VALID_START = "21-02-26 1100";
    private static final String VALID_END = "21-02-26 1500";
    private static final String OTHER_START = "22-02-26 1000";
    private static final String OTHER_END = "22-02-26 1200";

    private static PersonInformation infoOf(String name) {
        return new PersonInformation(new Name(name), null, null, null, null);
    }

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new DeleteEventCommand(null, VALID_START, VALID_END));
    }

    @Test
    public void constructor_nullStartTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new DeleteEventCommand(infoOf(VALID_NAME), null, VALID_END));
    }

    @Test
    public void constructor_nullEndTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new DeleteEventCommand(infoOf(VALID_NAME), VALID_START, null));
    }

    @Test
    public void execute_eventDeleted_success() throws Exception {
        Event eventToDelete = new Event("CS2103 Meeting", VALID_START, VALID_END);
        Event otherEvent = new Event("Lunch", OTHER_START, OTHER_END);

        Person person = new PersonBuilder().withName(VALID_NAME).build();
        person.addEvent(eventToDelete);
        person.addEvent(otherEvent);

        ModelStubWithPerson modelStub = new ModelStubWithPerson(person);
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), VALID_START, VALID_END);

        CommandResult result = command.execute(modelStub);

        assertEquals(String.format(DeleteEventCommand.MESSAGE_SUCCESS, VALID_NAME, eventToDelete),
                result.getFeedbackToUser());
        assertFalse(modelStub.editedPerson.getEvents().contains(eventToDelete));
        assertTrue(modelStub.editedPerson.getEvents().contains(otherEvent));
        assertEquals(1, modelStub.editedPerson.getEvents().size());
    }

    @Test
    public void execute_contactNotFound_throwsCommandException() {
        ModelStubWithNoPerson modelStub = new ModelStubWithNoPerson();
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), VALID_START, VALID_END);

        assertThrows(CommandException.class, Messages.MESSAGE_NO_MATCH, () ->
                command.execute(modelStub));
    }

    @Test
    public void execute_multipleContactsFound_throwsCommandException() {
        Person firstMatch = new PersonBuilder().withName(VALID_NAME).withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName(VALID_NAME).withPhone("90002222").build();

        ModelStubWithMultiplePersons modelStub =
                new ModelStubWithMultiplePersons(List.of(firstMatch, secondMatch));
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), VALID_START, VALID_END);

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () ->
                command.execute(modelStub));
    }

    @Test
    public void execute_eventNotFound_throwsCommandException() {
        Person person = new PersonBuilder().withName(VALID_NAME).build();
        ModelStubWithPerson modelStub = new ModelStubWithPerson(person);
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), VALID_START, VALID_END);

        assertThrows(CommandException.class, DeleteEventCommand.MESSAGE_EVENT_NOT_FOUND, () ->
                command.execute(modelStub));
    }

    @Test
    public void execute_startMatchesEndDiffers_throwsCommandException() throws Exception {
        Event event = new Event("CS2103 Meeting", VALID_START, VALID_END);
        Person person = new PersonBuilder().withName(VALID_NAME).build();
        person.addEvent(event);

        ModelStubWithPerson modelStub = new ModelStubWithPerson(person);
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), VALID_START, OTHER_END);

        assertThrows(CommandException.class, DeleteEventCommand.MESSAGE_EVENT_NOT_FOUND, () ->
                command.execute(modelStub));
    }

    @Test
    public void equals() {
        PersonInformation info = infoOf(VALID_NAME);
        DeleteEventCommand command = new DeleteEventCommand(info, VALID_START, VALID_END);
        DeleteEventCommand commandCopy = new DeleteEventCommand(info, VALID_START, VALID_END);
        DeleteEventCommand differentTime = new DeleteEventCommand(info, OTHER_START, OTHER_END);
        DeleteEventCommand differentPerson = new DeleteEventCommand(infoOf("Bob Choo"), VALID_START, VALID_END);

        // same object -> returns true
        assertTrue(command.equals(command));

        // same values -> returns true
        assertTrue(command.equals(commandCopy));

        // different types -> returns false
        assertFalse(command.equals(1));

        // null -> returns false
        assertFalse(command.equals(null));

        // different datetime -> returns false
        assertFalse(command.equals(differentTime));

        // different person -> returns false
        assertFalse(command.equals(differentPerson));
    }

    @Test
    public void toStringMethod() {
        PersonInformation info = infoOf(VALID_NAME);
        DeleteEventCommand command = new DeleteEventCommand(info, VALID_START, VALID_END);
        String expected = String.format("Deleting event for %s from %s to %s",
                info.name, VALID_START, VALID_END);
        assertEquals(expected, command.toString());
    }

    // ==================== Model Stubs ====================

    private class ModelStub implements Model {
        @Override public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public Path getAddressBookFilePath() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setAddressBookFilePath(Path addressBookFilePath) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ObservableList<Event> getFilteredEventList() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void updateFilteredEventList(Predicate<Event> predicate) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public Person findPersonByName(Name name) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public List<Person> findPersons(PersonInformation info) {
            throw new AssertionError("This method should not be called.");
        }
    }

    private class ModelStubWithPerson extends ModelStub {
        private final Person person;
        private Person editedPerson;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            if (person.getName().equals(info.name)) {
                return List.of(person);
            }
            return List.of();
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            this.editedPerson = editedPerson;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

    private class ModelStubWithNoPerson extends ModelStub {
        @Override
        public List<Person> findPersons(PersonInformation info) {
            return List.of();
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}
    }

    private class ModelStubWithMultiplePersons extends ModelStub {
        private final List<Person> persons;

        ModelStubWithMultiplePersons(List<Person> persons) {
            this.persons = persons;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            return persons;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}
    }
}
