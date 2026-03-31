package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.testutil.PersonBuilder;

public class CommandUtilTest {

    @Test
    public void targetPerson_singleMatch_returnsPerson() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        PersonInformation info = new PersonInformation(new Name("Alice Pauline"), null, null, null, Set.of());
        Person matched = CommandUtil.targetPerson(model, info);

        assertEquals(ALICE, matched);
    }

    @Test
    public void targetPerson_noMatch_throwsCommandException() {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        PersonInformation info = new PersonInformation(new Name("Nonexistent"), null, null, null, Set.of());

        assertThrows(CommandException.class, Messages.MESSAGE_NO_MATCH, () ->
                CommandUtil.targetPerson(model, info));
    }

    @Test
    public void targetPerson_multipleMatches_throwsCommandExceptionAndShowsMatchingPersons() {
        Model model = new ModelManager();
        Person first = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person second = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(first);
        model.addPerson(second);

        PersonInformation info = new PersonInformation(new Name("Alex Tan"), null, null, null, Set.of());

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () ->
                CommandUtil.targetPerson(model, info));

        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().contains(first));
        assertTrue(model.getFilteredPersonList().contains(second));
    }
}
