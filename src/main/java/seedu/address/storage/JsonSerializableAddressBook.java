package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_EVENT = "Events list contains duplicate event(s).";
    public static final String MESSAGE_CLASHING_EVENT = "Events list contains clashing (overlapping) event(s).";
    public static final String MESSAGE_DUPLICATE_PINNED_PERSON = "Pinned list contains duplicate person(s).";
    public static final String MESSAGE_PINNED_PERSON_NOT_IN_PERSONS =
            "Pinned list contains person(s) not present in persons list.";
    public static final String MESSAGE_ORPHANED_EVENT =
            "Dropping orphaned event with no linked persons: %s";
    public static final String MESSAGE_DUPLICATE_EVENT_ID =
            "Duplicate eventId %d found during load — skipping second entry.";
    public static final String MESSAGE_MISSING_EVENT_ID =
            "Event '%s' is missing eventId field — skipping.";
    private static final Logger LOGGER = LogsCenter.getLogger(JsonSerializableAddressBook.class);

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedEvent> events = new ArrayList<>();
    private final List<JsonAdaptedPerson> pinned = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and events.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
                                       @JsonProperty("events") List<JsonAdaptedEvent> events,
                                       @JsonProperty("pinned") List<JsonAdaptedPerson> pinned) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
        if (events != null) {
            this.events.addAll(events);
        }
        if (pinned != null) {
            this.pinned.addAll(pinned);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        events.addAll((source.getEventList().stream().map(JsonAdaptedEvent::new).collect(Collectors.toList())));
        pinned.addAll(source.getPinnedPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();

        Map<Integer, Event> eventMap = new HashMap<>();
        for (JsonAdaptedEvent jsonAdaptedEvent : events) {
            if (jsonAdaptedEvent.getEventId() == null) {
                Event event = jsonAdaptedEvent.toModelType();
                LOGGER.warning(String.format(MESSAGE_MISSING_EVENT_ID, event.getTitle()));
                continue;
            }
            Event event = jsonAdaptedEvent.toModelType();
            if (eventMap.containsKey(event.getEventId())) {
                LOGGER.warning(String.format(MESSAGE_DUPLICATE_EVENT_ID, event.getEventId()));
                continue;
            }
            boolean isOverlapping = eventMap.values().stream().anyMatch(event::isClashingWith);
            if (isOverlapping) {
                throw new IllegalValueException(MESSAGE_CLASHING_EVENT);
            }
            eventMap.put(event.getEventId(), event);
        }

        List<Person> loadedPersons = new ArrayList<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType(eventMap);
            boolean isDuplicate = loadedPersons.stream().anyMatch(person::isSamePerson);
            if (isDuplicate) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            loadedPersons.add(person);
        }

        // Rebuild numberOfPersonLinked from actual person-event references,
        // ignoring the saved counter which may be stale or tampered with.
        for (Person person : loadedPersons) {
            for (Event event : person.getEvents()) {
                event.incrementNumberOfPersonLinked();
            }
        }

        // Discard orphaned events (not referenced by any person) before adding persons.
        eventMap.values().removeIf(event -> {
            if (event.getNumberOfPersonLinked() == 0) {
                LOGGER.warning(String.format(MESSAGE_ORPHANED_EVENT, event.getTitle()));
                return true;
            }
            return false;
        });
        addressBook.setEvents(new ArrayList<>(eventMap.values()));

        for (Person person : loadedPersons) {
            addressBook.addPerson(person);
        }

        List<Person> pinnedPersons = new ArrayList<>();
        for (JsonAdaptedPerson jsonAdaptedPinnedPerson : pinned) {
            // Pass empty map — pinned entries are used only for identity matching,
            // not as a source of event data. The resolved person from loadedPersons is used instead.
            Person pinnedPerson = jsonAdaptedPinnedPerson.toModelType(Map.of());
            if (pinnedPersons.stream().anyMatch(pinnedPerson::isSamePerson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PINNED_PERSON);
            }
            // Ensure that every pinned person exists in the person list
            // Do not use addressBook.hasPerson()
            // Get the original person object from the person list - single source of truth
            Person existingPerson = addressBook.getPersonList().stream()
                    .filter(pinnedPerson::isSamePerson)
                    .findFirst()
                    .orElse(null);
            if (existingPerson == null) {
                throw new IllegalValueException(MESSAGE_PINNED_PERSON_NOT_IN_PERSONS);
            }
            pinnedPersons.add(existingPerson);
        }
        addressBook.setPinnedPersons(pinnedPersons);
        return addressBook;
    }

}
