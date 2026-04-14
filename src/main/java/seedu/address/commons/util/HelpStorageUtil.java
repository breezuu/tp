package seedu.address.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Messages;

/**
 * Utility class to handle the extraction and cleanup of offline help files.
 */
public class HelpStorageUtil {
    private static final Logger LOGGER = LogsCenter.getLogger(HelpStorageUtil.class);
    private static final String HELP_DIR_STRING = "data/help";

    /**
     * Extracts the help files from the JAR to the local data/help directory.
     */
    public static void copyOverOfflineHelp() throws IOException {
        List<String> fileNames = List.of("index.html", "index.css", "nab_app_logo.ico");
        copyFiles(fileNames);
    }

    /**
     * Copies the files required from the resource directory to the user's data/help directory
     */
    static void copyFiles(List<String> fileNames) throws IOException {
        Path userHelpDir = Paths.get(HELP_DIR_STRING);

        if (!Files.isDirectory(userHelpDir)) {
            Files.createDirectories(userHelpDir);
            LOGGER.info("Created default help directory at: " + userHelpDir.toAbsolutePath());
        }

        for (String file : fileNames) {
            try (InputStream inputStream = HelpStorageUtil.class.getResourceAsStream("/help/" + file)) {
                if (inputStream == null) {
                    LOGGER.severe("Could not find internal help file: /help/" + file);
                    throw new IOException(Messages.MESSAGE_MISSING_INTERNAL_RESOURCE + file);
                }

                Path copyTo = userHelpDir.resolve(file);
                Files.copy(inputStream, copyTo, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Copied " + file + " to " + copyTo.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.warning("Failed to copy " + file + ": " + e.getMessage());
                throw new IOException(Messages.MESSAGE_FAILED_OFFLINE_GUIDE + e.getMessage());
            }
        }
    }

    /**
     * Clears the entire data/help directory.
     */
    public static void clearDirectory() throws IOException {
        Path userHelpDir = Paths.get(HELP_DIR_STRING);

        try {
            FileUtil.clearDirectory(userHelpDir);
            LOGGER.info("Successfully cleared offline help directory.");
        } catch (IOException e) {
            LOGGER.severe("Clearing temporary directory failed: " + userHelpDir.toAbsolutePath());
            throw new IOException(Messages.MESSAGE_FAILED_OFFLINE_GUIDE + e.getMessage());
        }
    }
}
