package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Maintains command history state for navigating previous command inputs.
 */
public class CommandHistory {

    private static final Logger logger = LogsCenter.getLogger(CommandHistory.class);

    private final List<String> commandHistory;
    private int currentPointer; // Pointer to where the command would be stored in commandHistory
    private String currentCommand;

    /**
     * Constructs a CommandHistory with an empty history and pointer set to the end of the history list.
     */
    public CommandHistory() {
        commandHistory = new ArrayList<>();
        currentPointer = 0;
        currentCommand = "";
    }

    /**
     * Records a command entry and sets navigation to the newest position.
     *
     * @param commandText The command text to be recorded in history.
     */
    public void recordCommand(String commandText) {
        commandHistory.add(commandText);
        currentPointer = getCommandHistorySize() + 1; // Move pointer to the end of the history list
        // Clear the current command since a new command has been entered.
        currentCommand = "";
        logger.info("Recorded command in history. historySize=" + commandHistory.size()
                + ", pointer=" + currentPointer);
    }

    /**
     * Resets history navigation to the newest position if user types manually.
     *
     * @param currentText The current text in the command box to synchronize with.
     */
    public void syncWithUserInput(String currentText) {
        currentPointer = getCommandHistorySize() + 1;
        currentCommand = currentText;
    }

    /**
     * Navigates to an older command (Up key behavior).
     *
     * @param currentText The current text in the command box to return if history is empty or at the end.
     * @return The command text from history or the current text if history is empty.
     */
    public String navigateUp(String currentText) {
        assert currentPointer >= 0 : "Pointer should not be negative when navigating up.";
        assert currentPointer <= getCommandHistorySize() + 1
                : "Pointer should not exceed history size when navigating up.";

        if (commandHistory.isEmpty()) {
            return currentText;
        }

        if (currentPointer == getCommandHistorySize() + 1) {
            currentCommand = currentText;
        }

        if (currentPointer > 0) {
            currentPointer--;
        }

        logger.info("Navigate up to pointer=" + currentPointer);
        return commandHistory.get(currentPointer);
    }

    /**
     * Navigates to a newer command (Down key behavior).
     *
     * @param currentText The current text in the command box to return if history is empty or at the end.
     * @return The command text from history or the current text if history is empty or at the end.
     */
    public String navigateDown(String currentText) {
        assert currentPointer >= 0 : "Pointer should not be negative when navigating down.";
        assert currentPointer <= getCommandHistorySize() + 1
                : "Pointer should not exceed history size when navigating down.";

        if (commandHistory.isEmpty()) {
            return currentText;
        }

        if (currentPointer < getCommandHistorySize()) {
            currentPointer++;
            logger.info("Navigate down to pointer=" + currentPointer);
            return commandHistory.get(currentPointer);
        }

        if (currentPointer == getCommandHistorySize()) {
            currentPointer = getCommandHistorySize() + 1;
            logger.info("Navigate down returned current command. pointer=" + currentPointer);
            return currentCommand;
        }

        logger.info("Navigate down at latest input. pointer=" + currentPointer);
        return currentText;
    }

    private int getCommandHistorySize() {
        if (commandHistory.isEmpty()) {
            return 0;
        } else {
            return commandHistory.size() - 1;
        }
    }
}
