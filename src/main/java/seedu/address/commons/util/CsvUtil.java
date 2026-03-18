package seedu.address.commons.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvUtil {

    public static String sanitizeAndWrapValue(String value) {
        if (value == null) {
            return "\"\"";
        }

        String escapedQuotes = value.replace("\"", "\"\"");

        return "\"" + escapedQuotes + "\"";
    }

    public static String unwrapValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        // Remove leading and trailing quotes
        String unwrapped = value.replaceAll("^\"|\"$", "");

        // Restore double quotes
        return unwrapped.replace("\"\"", "\"");
    }

    public static String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }

        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}