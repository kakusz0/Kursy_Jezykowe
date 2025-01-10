package pl.kakusz.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.objects.Course;
import pl.kakusz.database.objects.User;
import pl.kakusz.fx.ControllerManager;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoginController {

    @FXML
    private ImageView logoImage;
    @FXML
    private Button mainButton, registerButton, forgotPasswordButton;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    public void initialize() {
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        emailField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                handleLogin();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                handleLogin();
            }
        });

    }

    @FXML
    private void handleRegisterButtonClick() throws IOException {
        ControllerManager.getInstance().getRegisterController().showWindow((Stage)registerButton.getScene().getWindow());
    }

    public void showWindow(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Login.fxml")));
        stage.setTitle("Logowanie");
        stage.setScene(new Scene(parent));
        stage.show();
        stage.centerOnScreen();
    }

    @FXML
    public void handleLogin() {
        if (validateFields()) {
            openWindow();
        }
    }


    private void openWindow() {
        Stage stage = (Stage) mainButton.getScene().getWindow();
        try {
            ControllerManager.getInstance().getDashBoardController().showWindow(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isEmailValid(String email) {
        return Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
                .matcher(email)
                .matches();
    }

    private boolean checkUserExistence(String username) {
        return DatabaseManager.getInstance().getUserManager().userExists(username);
    }

    private boolean checkEmailExistence(String email) {
        return DatabaseManager.getInstance().getUserManager().emailExists(email);
    }
    private boolean validateFields() {
        if (emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wszystkie pola muszą być wypełnione.");
            return false;
        }

        if (!isEmailValid(emailField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Adres email jest niepoprawny.");
            return false;
        }

        if (passwordField.getText().length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Hasło musi zawierać conajmniej 5 znaków.");
            return false;
        }

        User user = DatabaseManager.getInstance().getUserManager().getUserByEmail(emailField.getText());
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return false;
        }

        if (!BCrypt.checkpw(passwordField.getText(), user.getPassword())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Podane hasło jest niepoprawne.");
            return false;
        }

        DatabaseManager.getInstance().getUserManager().setCurrentUser(user);


        return true;
    }

    @FXML
    public void handleResetPasswordButtonClick() throws IOException {
        ControllerManager.getInstance().getResetPasswordController().showWindow((Stage)forgotPasswordButton.getScene().getWindow());
    }
}
