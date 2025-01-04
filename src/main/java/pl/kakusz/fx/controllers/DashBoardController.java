package pl.kakusz.fx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.hibernate.query.Query;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.managers.UserManager;
import pl.kakusz.database.objects.Course;
import pl.kakusz.fx.ControllerManager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class DashBoardController {
    @FXML
    public Button logoutButton, myCoursesButton, buyCourseButton, transactionsButton;
    @FXML
    public VBox adminLayout;


    @FXML
    private Label usernameLabel, idKontaLabel;

    @FXML
    private ImageView logoImage;

    @FXML
    private VBox mainContainer, courseContainer, courseVBox;
    @FXML
    private ScrollPane scrollCourse;

    @FXML
    public void initialize() throws IOException {
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        usernameLabel.setText("Witaj, " + DatabaseManager.getInstance().getUserManager().getCurrentUser().getUsername() + "!");
        idKontaLabel.setText("ID konta: " + DatabaseManager.getInstance().getUserManager().getCurrentUser().getId());
        if (DatabaseManager.getInstance().getUserManager().getCurrentUser() != null && DatabaseManager.getInstance().getUserManager().getCurrentUser().getRole().equalsIgnoreCase("admin")) {
            Button adminButton = new Button("Admin");
            adminButton.setStyle("-fx-text-fill: white; -fx-background-color: #2e3b54; -fx-background-radius: 5;");
            adminButton.setOnMouseClicked(event -> handleAdmin());
            adminLayout.getChildren().add(adminButton);
        }
        for (Course course : DatabaseManager.getInstance().getCourseManager().getCourseList()) {
            VBox courseButton = new VBox(10); // Ustalamy odstęp między elementami w VBox
            courseButton.setStyle("-fx-background-color: #2e3b54; -fx-background-radius: 10px; -fx-padding: 15px;");


            // Wyśrodkowany tekst z nazwą kursu
            Label label = new Label(course.getName());
            label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);  // Wyśrodkowanie tekstu

            // Opis kursu
            Label description = new Label(course.getDescription());
            description.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-wrap-text: true;");
            description.setMaxWidth(Double.MAX_VALUE);
            description.setAlignment(Pos.CENTER); // Wyśrodkowanie tekstu

            // Przyciski z nowym stylem i wyśrodkowanym tekstem
            Button button = new Button("Kup za " + course.getPrice() + "zł");
            button.setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50; -fx-background-radius: 5px; -fx-font-size: 14px;");
            button.setPrefWidth(200);  // Preferowana szerokość przycisku
            button.setMaxWidth(200); // Przyciski wypełniają dostępne miejsce, ale są ograniczone maxWidth
            button.setAlignment(Pos.CENTER);

            // Dodanie elementów do VBox
            courseButton.getChildren().addAll(label, description, button);

            // Wyśrodkowanie VBox w kontenerze
            courseButton.setAlignment(Pos.CENTER);

            // Kliknięcie na kurs - akcja może być dodana w tej części
            courseButton.setOnMouseClicked(event -> {
                // Można dodać akcję, np. przejście do strony kursu
            });

            // Dodanie kursu do głównego kontenera
            courseVBox.getChildren().add(courseButton);
        }


    }

    public void handleAdmin() {
        mainContainer.setVisible(false);
        courseContainer.setVisible(false);
    }


    public void showWindow(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));
        stage.setTitle("Panel główny");
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void handleBuyCourseClick() throws IOException {
        mainContainer.setVisible(false);
        courseContainer.setVisible(true);


    }

    public void handleHomeClick() throws IOException {
        mainContainer.setVisible(true);
        courseContainer.setVisible(false);
    }


    public void handleLogout() {
        DatabaseManager.getInstance().getUserManager().setCurrentUser(null);
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        try {
            ControllerManager.getInstance().getLoginController().showWindow(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
