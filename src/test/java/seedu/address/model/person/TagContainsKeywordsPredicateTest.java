package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TagContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = Collections.singletonList("first");
        List<String> secondPredicateKeywordList = Arrays.asList("first", "second");

        TagContainsKeywordsPredicate firstPredicate = new TagContainsKeywordsPredicate(firstPredicateKeywordList);
        TagContainsKeywordsPredicate secondPredicate = new TagContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        TagContainsKeywordsPredicate firstPredicateCopy = new TagContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_tagContainsKeywords_returnsTrue() {
        // One keyword matching one tag
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(Arrays.asList("Friends"));
        assertTrue(predicate.test(new PersonBuilder().withTags("Friends").build()));

        // Multiple keywords, with one matching a tag
        predicate = new TagContainsKeywordsPredicate(Arrays.asList("Friends", "Colleagues"));
        assertTrue(predicate.test(new PersonBuilder().withTags("Friends").build()));

        // Person has multiple tags, with one matching the keyword
        predicate = new TagContainsKeywordsPredicate(Arrays.asList("OWE_MONEY"));
        assertTrue(predicate.test(new PersonBuilder().withTags("Friends", "OWE_MONEY").build()));

        // Mixed-case keywords (The tag "CS2103 group" VS the tag "CS2103 Group")
        predicate = new TagContainsKeywordsPredicate(Arrays.asList("CS2103 group"));
        assertTrue(predicate.test(new PersonBuilder().withTags("CS2103 Group").build()));
    }

    @Test
    public void test_tagDoesNotContainKeywords_returnsFalse() {
        // Partial match (keyword is "CS" but the tag is "CS2103 Group")
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(Arrays.asList("CS"));
        assertFalse(predicate.test(new PersonBuilder().withTags("CS2103 Group").build()));

        // Keyword with spaces VS separate tags
        // If user types "CS2103 Group" as one keyword, it should not be considered as separate tags ("CS2103", "Group")
        predicate = new TagContainsKeywordsPredicate(Arrays.asList("CS2103 Group"));
        assertFalse(predicate.test(new PersonBuilder().withTags("CS2103", "Group").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(keywords);

        String expected = TagContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
