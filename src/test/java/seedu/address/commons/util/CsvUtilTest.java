package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CsvUtilTest {
    @Test
    public void sanitizeValue_containsComma_wrappedCorrectly() {
        String input = "Ng, David";
        assertEquals("\"Ng, David\"", CsvUtil.sanitizeAndWrapValue(input));
    }

    @Test
    public void unwrapValue_escapedQuotes_restoredCorrectly() {
        String input = "\"Yik \"\"The Programmer\"\" Leong\"";
        assertEquals("Yik \"The Programmer\" Leong", CsvUtil.unwrapValue(input));
    }

    @Test
    public void splitCsvLine_complexRow_splitsCorrectly() {
        String row = "\"David\",\"91234567\",\"test@example.com\",\"Blk 123, Jurong\",\"tag1;tag2\"";
        String[] expected = {"\"David\"", "\"91234567\"", "\"test@example.com\"", "\"Blk 123, Jurong\"", "\"tag1;tag2\""};
        assertArrayEquals(expected, CsvUtil.splitCsvLine(row));
    }
}
