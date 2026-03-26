package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.event.Event;
import seedu.address.model.event.UniqueEventList;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed for persons (by {@code Person#isSamePerson(Person)})
 * and events (by {@code Event#isSameEvent(Event)}).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons;
    private final UniqueEventList events;

    /*
     * The 'unusual' code block below is a non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        persons = new UniquePersonList();
        events = new UniqueEventList();
    }

    /**
     * Creates an empty AddressBook.
     */
    public AddressBook() {}

    /**
     * Creates an AddressBook using the persons and events in {@code toBeCopied}.
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        setPersons(newData.getPersonList());
        setEvents(newData.getEventList());
    }


    //// person-level operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// event-level operations

    /**
     * Replaces the contents of the event list with {@code events}.
     * {@code events} must not contain duplicate events.
     */
    public void setEvents(List<Event> events) {
        this.events.setEvents(events);
    }

    /**
     * Returns true if an event with the same identity as {@code event} exists in the address book.
     */
    public boolean hasEvent(Event event) {
        requireNonNull(event);
        return events.contains(event);
    }

    /**
     * Returns true if any event in the address book overlaps with {@code event}.
     */
    public boolean hasOverlappingEvent(Event event) {
        requireNonNull(event);
        return events.hasOverlappingEvent(event);
    }

    /**
     * Adds an event to the address book.
     * The event must not already exist in the address book.
     */
    public void addEvent(Event e) {
        events.add(e);
    }

    /**
     * Replaces the given event {@code target} in the list with {@code editedEvent}.
     * {@code target} must exist and {@code editedEvent} must not duplicate another event.
     */
    public void setEvent(Event target, Event editedEvent) {
        requireNonNull(editedEvent);
        events.setEvent(target, editedEvent);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeEvent(Event key) {
        events.remove(key);
    }

    /**
     * Finds the existing event with the same identity as {@code eventToLink},
     * increments its linked-person count, and returns it.
     *
     * @throws java.util.NoSuchElementException if no matching event exists
     */
    public Event linkPersonToEvent(Event eventToLink) {
        requireNonNull(eventToLink);

        Event existingEvent = events.asUnmodifiableObservableList()
                .stream()
                .filter(eventToLink::isSameEvent)
                .findFirst()
                .orElseThrow();

        existingEvent.incrementNumberOfPersonLinked();
        return existingEvent;
    }

    /**
     * Finds the existing event with the same identity as {@code eventToUnlink},
     * decrements its linked-person count, removes it if the count reaches zero,
     * and returns it.
     *
     * @throws java.util.NoSuchElementException if no matching event exists
     */
    public Event unlinkPersonFromEvent(Event eventToUnlink) {
        requireNonNull(eventToUnlink);

        Event existingEvent = events.asUnmodifiableObservableList()
                .stream()
                .filter(eventToUnlink::isSameEvent)
                .findFirst()
                .orElseThrow();

        existingEvent.decrementNumberOfPersonLinked();

        if (existingEvent.getNumberOfPersonLinked() == 0) {
            events.remove(existingEvent);
        }

        return existingEvent;
    }


    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("events", events)
                .toString();
    }

    /**
     * Returns an unmodifiable view of the person list.
     */
    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    /**
     * Returns an unmodifiable view of the event list.
     */
    @Override
    public ObservableList<Event> getEventList() {
        return events.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AddressBook otherAddressBook) {
            return persons.equals(otherAddressBook.persons)
                    && events.equals(otherAddressBook.events);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, events);
    }
}
