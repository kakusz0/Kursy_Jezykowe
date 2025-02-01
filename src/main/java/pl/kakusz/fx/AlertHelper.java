package pl.kakusz.fx;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertHelper {
    /**
     * Wyświetla alert z komunikatem o podanym typie.
     *
     * @param alertType typ alertu (np. informacja, błąd).
     * @param title     tytuł alertu.
     * @param message   treść komunikatu do wyświetlenia.
     */

    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
