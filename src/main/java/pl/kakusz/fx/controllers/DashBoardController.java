package pl.kakusz.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.objects.Course;
import pl.kakusz.database.objects.User;
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
    private VBox mainContainer, courseContainer, courseVBox, transactionsContainer;

    @FXML
    private TextField amountField;

    @FXML
    public void initialize() {
        // Inicjalizacja logo
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));

        // Pobranie użytkownika z załadowanymi kursami
        User currentUser = DatabaseManager.getInstance().getUserManager()
                .getCurrentUserWithCourses(DatabaseManager.getInstance().getUserManager().getCurrentUser().getId());

        // Wyświetlenie informacji o użytkowniku
        usernameLabel.setText("Witaj, " + currentUser.getUsername() + "!");
        idKontaLabel.setText("ID konta: " + currentUser.getId());

        // Dodanie przycisku Admin, jeśli użytkownik ma rolę admina
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            Button adminButton = new Button("Admin");
            adminButton.setStyle("-fx-text-fill: white; -fx-background-color: #2e3b54; -fx-background-radius: 5;");
            adminButton.setOnMouseClicked(event -> handleAdmin());
            adminLayout.getChildren().add(adminButton);
        }

        // Tworzenie przycisków kursów
        createCourseButtons(currentUser);
    }

    // Metoda do tworzenia przycisków kursów
    private void createCourseButtons(User currentUser) {
        // Pobranie pełnej listy kursów
        List<Course> allCourses = DatabaseManager.getInstance().getCourseManager().getCourseList();

        for (Course course : allCourses) {
            VBox courseButton = new VBox(10);
            courseButton.setStyle("-fx-background-color: #2e3b54; -fx-background-radius: 10px; -fx-padding: 15px;");

            // Dodanie nazwy kursu
            Label label = new Label(course.getName());
            label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
            label.setAlignment(Pos.CENTER);

            // Dodanie opisu kursu
            Label description = new Label(course.getDescription());
            description.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
            description.setAlignment(Pos.CENTER);

            // Dodanie przycisku do kursu
            Button button;
            if (currentUser.getCourses().contains(course)) {
                button = new Button("Posiadasz kurs");
                button.setStyle("-fx-text-fill: white; -fx-background-color: #4275f5; -fx-background-radius: 5px; -fx-font-size: 14px;");
            } else {
                button = new Button("Kup za " + course.getPrice() + " zł");
                button.setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50; -fx-background-radius: 5px; -fx-font-size: 14px;");
            }

            button.setPrefWidth(200);
            button.setAlignment(Pos.CENTER);

            // Dodanie elementów do struktury VBox
            courseButton.getChildren().addAll(label, description, button);
            courseButton.setAlignment(Pos.CENTER);

            // Dodanie przycisku do głównego kontenera
            courseVBox.getChildren().add(courseButton);
        }
    }

    // Obsługa kliknięcia przycisku Admin
    public void handleAdmin() {
        mainContainer.setVisible(false);
        courseContainer.setVisible(false);
        transactionsContainer.setVisible(false);
    }

    // Wyświetlenie głównego okna aplikacji
    public void showWindow(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));
        stage.setTitle("Panel główny");
        stage.setScene(new Scene(parent));
        stage.show();
    }

    // Obsługa kliknięcia przycisku "Kup Kursy"
    public void handleBuyCourseClick() throws IOException {
        mainContainer.setVisible(false);
        courseContainer.setVisible(true);
        transactionsContainer.setVisible(false);
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


    @FXML
    public void handleAddFunds() {
        String amountInput = amountField.getText();

        try {
            // Spróbuj zparsować wartość z pola na liczbę
            double amount = Double.parseDouble(amountInput);

            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Kwota musi być większa od zera.");
                return;
            }

            // Pobierz aktualnego użytkownika
            User currentUser = DatabaseManager.getInstance().getUserManager().getCurrentUser();

            // Zwiększ saldo użytkownika
            currentUser.setBalance(currentUser.getBalance() + amount);

            // Zapisz w bazie danych (używając metody w UserManager)
            DatabaseManager.getInstance().getUserManager().updateUserBalance(currentUser);


            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Doładowano konto o " + amount + "zł.");

            amountField.clear();

        } catch (NumberFormatException e) {
            // Jeśli wpisany tekst nie jest liczbą

            showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawna liczba. Wprowadź liczbę.");
        } catch (Exception e) {
            // Obsługa innych nieoczekiwanych wyjątków
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd podczas doładowania.");
            e.printStackTrace();
        }
    }

    // Obsługa kliknięcia przycisku "Transakcje"
    public void handleTransactionsClick() throws IOException {
        mainContainer.setVisible(false);
        courseContainer.setVisible(false);
        transactionsContainer.setVisible(true);
    }

    // Obsługa przycisku powrotu do głównej strony
    public void handleHomeClick() throws IOException {
        mainContainer.setVisible(true);
        courseContainer.setVisible(false);
        transactionsContainer.setVisible(false);
    }

    // Obsługa przycisku Wyloguj
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