package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_missingPersonsAndEventsFields_success() throws Exception {
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString("{}", JsonSerializableAddressBook.class);

        AddressBook addressBook = dataFromJson.toModelType();
        assertEquals(0, addressBook.getPersonList().size());
        assertEquals(0, addressBook.getEventList().size());
    }

    @Test
    public void toModelType_duplicateEvents_throwsIllegalValueException() throws Exception {
        String json = """
                {
                  "persons": [],
                  "events": [
                    {
                      "title": "Project Review",
                      "description": "Review scope",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 2
                    },
                    {
                      "title": "Project Review",
                      "description": "Another description",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 7
                    }
                  ]
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_EVENT,
                dataFromJson::toModelType);
    }

    @Test
    public void toModelType_personEventsReuseTopLevelEventInstances() throws Exception {
        String json = """
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": "alice@example.com",
                      "address": "123, Jurong West Ave 6, #08-111",
                      "tags": ["friends"],
                      "events": [
                        {
                          "title": "Project Review",
                          "description": "Review scope",
                          "startTime": "2026-03-25 0900",
                          "endTime": "2026-03-25 1000",
                          "numberOfPersonLinked": 1
                        }
                      ]
                    }
                  ],
                  "events": [
                    {
                      "title": "Project Review",
                      "description": "Review scope",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 4
                    }
                  ]
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        AddressBook addressBook = dataFromJson.toModelType();
        var topLevelEvent = addressBook.getEventList().get(0);
        var linkedEvent = addressBook.getPersonList().get(0).getEvents().get(0);

        assertSame(topLevelEvent, linkedEvent);
        assertEquals(4, linkedEvent.getNumberOfPersonLinked());
    }

}
