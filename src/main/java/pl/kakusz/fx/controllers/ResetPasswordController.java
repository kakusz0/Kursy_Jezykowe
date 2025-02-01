package pl.kakusz.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.managers.UserManager;
import pl.kakusz.database.objects.User;
import pl.kakusz.fx.AlertHelper;
import pl.kakusz.fx.ControllerManager;

import java.io.IOException;
import java.util.Objects;

public class ResetPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Button backToLoginButton;

    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
    }

    public void showWindow(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ResetPassword.fxml")));
        stage.setTitle("Logowanie");
        stage.setScene(new Scene(parent));
        stage.show();
        stage.centerOnScreen();
    }

    @FXML
    private void handleResetPassword() {

        if (emailField.getText().isEmpty() || oldPasswordField.getText().isEmpty() || passwordField.getText().isEmpty() || repeatPasswordField.getText().isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR,"Błąd", "Wszystkie pola muszą być wypełnione.");
            return;
        }

        if (!passwordField.getText().equals(repeatPasswordField.getText())) {
            AlertHelper.showAlert(AlertType.ERROR ,"Błąd", "Nowe hasła nie są zgodne.");
            return;
        }

        String email = emailField.getText();

        UserManager userManager = DatabaseManager.getInstance().getUserManager();
        User user = userManager.getUserByEmail(email);
        if (user == null) {
            AlertHelper.showAlert(AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        String oldPassword = oldPasswordField.getText();
        boolean success = userManager.updatePassword(email, oldPassword, passwordField.getText());
        if (success) {
            AlertHelper.showAlert(AlertType.CONFIRMATION, "Sukces", "Hasło zostało zresetowane pomyślnie.");
            clearFields();
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Błąd", "Stare hasło jest niepoprawne. Spróbuj ponownie.");
        }
    }


    private void clearFields() {
        emailField.clear();
        oldPasswordField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
    }

    @FXML
    private void handleBackToLogin() throws IOException {
        ControllerManager.getInstance().getLoginController().showWindow((Stage) backToLoginButton.getScene().getWindow());
    }
}
