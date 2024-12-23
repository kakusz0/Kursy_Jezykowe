package pl.kakusz.fx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.fx.ControllerManager;

import java.io.IOException;
import java.util.Objects;

public class DashBoardController {

    public Button logoutButton;

    @FXML
    private Label usernameLabel, idKontaLabel;

    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        usernameLabel.setText("Witaj, " + DatabaseManager.getInstance().getCurrentUser().getUsername() + "!");
        idKontaLabel.setText("ID konta: " + DatabaseManager.getInstance().getCurrentUser().getId());
    }


    public void showWindow(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));
        stage.setTitle("Panel główny");
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        try {
            ControllerManager.getInstance().getLoginController().showWindow(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
