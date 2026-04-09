package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Alert;

public class UiManagerTest {

    @Test
    public void showDataLoadingError_displaysWarningDialogWithExpectedText() {
        TestUiManager uiManager = new TestUiManager();

        uiManager.showDataLoadingError("Persons list contains duplicate person(s).");

        assertEquals(Alert.AlertType.WARNING, uiManager.type);
        assertEquals("Data Loading Error", uiManager.title);
        assertEquals("Your data file could not be loaded. Starting with an empty address book.",
                uiManager.headerText);
        assertEquals("Persons list contains duplicate person(s).", uiManager.contentText);
    }

    private static class TestUiManager extends UiManager {
        private Alert.AlertType type;
        private String title;
        private String headerText;
        private String contentText;

        private TestUiManager() {
            super(null);
        }

        @Override
        void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
            this.type = type;
            this.title = title;
            this.headerText = headerText;
            this.contentText = contentText;
        }
    }
}
