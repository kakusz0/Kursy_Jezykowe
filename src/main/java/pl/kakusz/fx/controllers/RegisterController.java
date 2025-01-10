package pl.kakusz.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.objects.User;
import pl.kakusz.fx.ControllerManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegisterController {

    @FXML
    private ImageView logoImage, captchaImage;
    @FXML
    private TextField captchaField, usernameField, emailField;
    @FXML
    private PasswordField passwordField, repeatPasswordField;
    @FXML
    private CheckBox termsCheckBox;
    @FXML
    private Button registerButton, backToLoginButton;
    @FXML
    private Hyperlink privacyPolicyLink;


    @FXML
    public void initialize() {
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
    }


    @FXML
    public void handleRegister() {
        if (validateFields()) {
            User newUser = createUser();
            saveUser(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Rejestracja zakończona pomyślnie!");

            openWindow();
        }
    }

    private User createUser() {
        User newUser = new User();
        newUser.setUsername(usernameField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(12)));
        newUser.setBalance(0.0);
        newUser.setRole("USER");
        return newUser;
    }

    private void saveUser(User user) {
        DatabaseManager.getInstance().getUserManager().saveUser(user);
    }

    private boolean validateFields() {
        if (usernameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty() || repeatPasswordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wszystkie pola muszą być wypełnione.");
            return false;
        }

        if (checkUserExistence(usernameField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nazwa użytkownika jest już zajęta.");
            return false;
        }

        if (!isEmailValid(emailField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Adres email jest niepoprawny.");
            return false;
        }

        if (checkEmailExistence(emailField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Adres email jest już używany.");
            return false;
        }

        if (passwordField.getText().length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Hasło musi zawierać conajmniej 5 znaków.");
            return false;
        }

        if (!passwordField.getText().equals(repeatPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Hasła nie są identyczne.");
            return false;
        }

        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Musisz zaakceptować regulamin.");
            return false;
        }


        return true;
    }

    private boolean checkUserExistence(String username) {
        return DatabaseManager.getInstance().getUserManager().userExists(username);
    }

    private boolean checkEmailExistence(String email) {
        return DatabaseManager.getInstance().getUserManager().emailExists(email);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private boolean isEmailValid(String email) {
        return Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
                .matcher(email)
                .matches();
    }

    private void openWindow() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        try {
            ControllerManager.getInstance().getLoginController().showWindow(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @FXML
    private void handlePrivacyPolicyClick() throws IOException {
        ControllerManager.getInstance().getPrivacyPolicyController().showWindow((Stage) privacyPolicyLink.getScene().getWindow());
    }


    public void showWindow(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
        Parent root = loader.load();
        stage.setTitle("Rejestracja");
        stage.setScene(new Scene(root));
        stage.show();
        stage.centerOnScreen();
    }

    @FXML
    public void handleBackToLogin() throws IOException {
        ControllerManager.getInstance().getLoginController().showWindow((Stage) backToLoginButton.getScene().getWindow());
    }
}
