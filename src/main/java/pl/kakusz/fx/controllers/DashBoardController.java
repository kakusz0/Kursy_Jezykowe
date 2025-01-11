package pl.kakusz.fx.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.objects.Course;
import pl.kakusz.database.objects.User;
import pl.kakusz.fx.ControllerManager;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class DashBoardController {
    @FXML
    private VBox adminLayout, mainContainer, myCoursesContainer, adminContainer, courseContainer, transactionsContainer, courseVBox;
    @FXML
    private TextField amountField, searchField, courseNameField, courseDescriptionField, coursePriceField, courseLinkField;
    @FXML
    private TextField userEmailField, courseIdField, passwordField, deleteCourseIdField;
    @FXML
    FlowPane coursePane;
    @FXML
    private Pagination pagination;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private Label usernameLabel, idKontaLabel, accountBalanceLabel;
    @FXML
    private ImageView logoImage, logoImage2;
    @FXML
    private Button logoutButton, openCourseButton;
    @FXML
    private TableView<Course> myCoursesTable;
    @FXML
    private TableColumn<Course, String> productColumn, priceColumn, descriptionColumn;

    private List<Course> allCourses;
    private ObservableList<Course> filteredCourseList;
    private static final int ITEMS_PER_PAGE = 5;
    private User currentUser;

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        logoImage2.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        currentUser = DatabaseManager.getInstance().getUserManager()
                .getCurrentUserWithCourses(DatabaseManager.getInstance().getUserManager().getCurrentUser().getId());

        usernameLabel.setText("Witaj, " + currentUser.getUsername() + "!");
        idKontaLabel.setText("ID konta: " + currentUser.getId());
        accountBalanceLabel.setText("Stan konta: " + String.format("%.2f", currentUser.getBalance()) + " zł");

        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            createAdminButton();
        }

        allCourses = DatabaseManager.getInstance().getCourseManager().getCourseList();
        filteredCourseList = FXCollections.observableArrayList(allCourses);
        sortComboBox.getItems().addAll("Sortuj według ceny: od największej", "Sortuj według ceny: od najmniejszej");
        sortComboBox.setValue(sortComboBox.getItems().get(0));
        setupPagination();


        productColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        productColumn.setStyle("-fx-alignment: CENTER;");
        descriptionColumn.setStyle("-fx-alignment: CENTER;");
        priceColumn.setStyle("-fx-alignment: CENTER;");

        openCourseButton.setOnAction(event -> openSelectedCourse());

        populateRandomCoursesPane(allCourses);
    }

    private void createAdminButton() {
        Button adminButton = new Button("Admin");
        adminButton.setStyle("-fx-text-fill: white; -fx-background-color: #2e3b54; -fx-background-radius: 5;");
        adminButton.setOnMouseClicked(event -> toggleVisibility(false, false, false, false, true));
        adminLayout.getChildren().add(adminButton);
    }

    @FXML
    private void filterCourses() {
        String searchText = searchField.getText().toLowerCase();
        String selectedSort = sortComboBox.getValue();

        filteredCourseList.setAll(allCourses.stream()
                .filter(course -> course.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList()));

        if (selectedSort != null) {
            if (selectedSort.contains("największej")) {
                filteredCourseList.sort((c1, c2) -> Double.compare(c2.getPrice(), c1.getPrice()));
            } else {
                filteredCourseList.sort(Comparator.comparingDouble(Course::getPrice));
            }
        }

        setupPagination();
    }

    private void setupPagination() {
        pagination.setPageCount((int) Math.ceil((double) filteredCourseList.size() / ITEMS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);
        pagination.setPageFactory(this::createPage);
        showPage(0);
    }

    private VBox createPage(int pageIndex) {
        return showPage(pageIndex);
    }

    private VBox showPage(int pageIndex) {
        courseVBox.getChildren().clear();
        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredCourseList.size());

        for (int i = start; i < end; i++) {
            createCourseButton(filteredCourseList.get(i));
        }
        pagination.setCurrentPageIndex(pageIndex);

        pagination.setPrefHeight(Integer.MAX_VALUE);
        return courseVBox;
    }

    private List<Course> pickRandomCourse(List<Course> courseList) {
        Random random = new Random();
        int randomIndex = random.nextInt(courseList.size());
        return Collections.singletonList(courseList.get(randomIndex));
    }
    public void populateRandomCoursesPane(List<Course> allCourses) {
        // Wyczyść obecnie wyświetlane kursy (jeśli istnieją)
        coursePane.getChildren().clear();

        // Pobierz losowe kursy (przykładowo 3)
        for (int i = 0; i < 3; i++) {
            List<Course> pickedCourses = pickRandomCourse(allCourses);
            pickedCourses.forEach(this::createPopularCourse);
        }
    }
    private void createPopularCourse(Course course) {
        VBox coursesBox = new VBox(10);


        boolean hasCourse = currentUser.getCourses().stream().anyMatch(c -> c.getId().equals(course.getId()));


        if (hasCourse) {
            coursesBox.setStyle("-fx-background-color: #7a7a7a; -fx-padding: 15px;");
        } else {
            coursesBox.setStyle("-fx-background-color: #2e3b54; -fx-padding: 15px;");
        }

        Label label = new Label(course.getName());
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label description = new Label(course.getDescription());
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Button button = new Button(hasCourse ? "Posiadasz kurs" : "Kup za " + course.getPrice() + " zł");
        button.setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50; -fx-background-radius: 5px; -fx-font-size: 14px;");
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER);


        if (hasCourse) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> handleBuyCourse(course.getId()));
        }

        coursesBox.getChildren().addAll(label, description, button);
        coursesBox.setAlignment(Pos.CENTER);

        // Dodanie kursu do kontenera `coursePane`
        coursePane.getChildren().add(coursesBox);
    }

    private void createCourseButton(Course course) {
        VBox coursesBox = new VBox(10);


        boolean hasCourse = currentUser.getCourses().stream().anyMatch(c -> c.getId().equals(course.getId()));


        if (hasCourse) {
            coursesBox.setStyle("-fx-background-color: #7a7a7a; -fx-padding: 15px;");
        } else {
            coursesBox.setStyle("-fx-background-color: #2e3b54; -fx-padding: 15px;");
        }

        Label label = new Label(course.getName());
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label description = new Label(course.getDescription());
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Button button = new Button(hasCourse ? "Posiadasz kurs" : "Kup za " + course.getPrice() + " zł");
        button.setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50; -fx-background-radius: 5px; -fx-font-size: 14px;");
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER);


        if (hasCourse) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> handleBuyCourse(course.getId()));
        }

        coursesBox.getChildren().addAll(label, description, button);
        coursesBox.setAlignment(Pos.CENTER);
        this.courseVBox.getChildren().add(coursesBox);
    }


    public void showWindow(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));
        stage.setTitle("Panel główny");
        stage.setMinHeight(1000);
        stage.setMinWidth(930);
        stage.setScene(new Scene(parent));
        stage.show();
        stage.centerOnScreen();
    }

    private void handleBuyCourse(Long courseId) {


        Course course = DatabaseManager.getInstance().getCourseManager().getCourseById(courseId);

        if (currentUser.getBalance() < course.getPrice()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie masz wystarczających środków na koncie.");
            return;
        }


        currentUser.getCourses().add(course);


        currentUser.setBalance(currentUser.getBalance() - course.getPrice());

        DatabaseManager.getInstance().getUserManager().updateUserBalance(currentUser);


        accountBalanceLabel.setText("Stan konta: " + String.format("%.2f", currentUser.getBalance()) + " zł");

        setupPagination();

        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zakupiono kurs: " + course.getName());
    }

    @FXML
    public void handleBuyCourseClick() {
        accountBalanceLabel.setText("Stan konta: " + String.format("%.2f", currentUser.getBalance()) + " zł");
        toggleVisibility(true, false, false, false, false);
    }


    @FXML
    private void handleMyCoursesClick() {
        ObservableList<Course> courses = FXCollections.observableArrayList(
                currentUser.getCourses()
        );


        myCoursesTable.setItems(courses);


        toggleVisibility(false, false, false, true, false);
    }

    private void toggleVisibility(boolean showCourses, boolean showMain, boolean showTransactions, boolean showMyCourses, boolean showAdmin) {
        courseContainer.setVisible(showCourses);
        mainContainer.setVisible(showMain);
        transactionsContainer.setVisible(showTransactions);
        myCoursesContainer.setVisible(showMyCourses);
        adminContainer.setVisible(showAdmin);
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
        processFunds(amountField.getText());
    }

    private void processFunds(String amountInput) {
        try {
            double amount = Double.parseDouble(amountInput);

            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Kwota musi być większa od zera.");
                return;
            }

            currentUser.setBalance(currentUser.getBalance() + amount);
            DatabaseManager.getInstance().getUserManager().updateUserBalance(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Doładowano konto o " + amount + " zł.");

            amountField.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawna liczba. Wprowadź liczbę.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd podczas doładowania.");
            e.printStackTrace();
        }
    }

    public void handleTransactionsClick() {
        toggleVisibility(false, false, true, false, false);
    }

    public void handleHomeClick() {
        toggleVisibility(false, true, false, false, false);
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

    @FXML
    private void handleAddCourse() {
        String name = courseNameField.getText();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Podaj nazwe kursu.");
            return;
        }
        String description = courseDescriptionField.getText();
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Podaj opis kursu.");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(coursePriceField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawna cena. Wprowadź liczbe.");
            return;
        }
        String link = courseLinkField.getText();
        if (link.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Podaj link do kursu.");
            return;
        }
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setPrice(price);
        course.setLink(link);

        DatabaseManager.getInstance().getCourseManager().addCourse(course);
        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kurs został dodany.");
        System.out.println("Dodano kurs: " + name);
    }

    @FXML
    private void handleDeleteUser() {
        String userEmail = (userEmailField.getText());
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);
        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");

        } else if (userByEmail.getEmail().equals(currentUser.getEmail())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie možesz usunięć siebie.");
        } else {
            DatabaseManager.getInstance().getUserManager().deleteUser(userByEmail);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik został usunięty.");
        }

        System.out.println("Usunięto użytkownika o nazwie email: " + userEmail);
    }

    @FXML
    private void handleResetPassword() {
        String userEmail = (userEmailField.getText());
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);
        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
        } else {
            DatabaseManager.getInstance().getUserManager().updatePassword(userEmail, userByEmail.getPassword(), passwordField.getText());
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Hasło zostało zmienione.");
        }
        System.out.println("Zmieniono hasło użytkownika o nazwie email: " + userEmail);
    }

    @FXML
    private void handleAssignCourse() {
        String userEmail = (userEmailField.getText());
        String courseName = (courseIdField.getText());
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);
        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
        } else {
            DatabaseManager.getInstance().getUserManager().handleAssignCourse(userByEmail, courseName);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Przypisano kurs " + courseName + " użytkownikowi o nazwie email" + userEmail);
        }
        System.out.println("Przypisano kurs " + courseName + " użytkownikowi o nazwie email" + userEmail);
    }

    @FXML
    private void handleRemoveCourse() {
        String userEmail = (userEmailField.getText());
        String courseName = (courseIdField.getText());
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);
        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
        } else {
            DatabaseManager.getInstance().getUserManager().handleRemoveCourse(userByEmail, courseName);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Przypisano kurs " + courseName + " użytkownikowi o nazwie email" + userEmail);
        }
        System.out.println("Usunięto kurs " + courseName + " z użytkownika " + userEmail);
    }


    @FXML
    private void handleDeleteCourse() {
        try {
            String courseName = (deleteCourseIdField.getText());
            Course course = DatabaseManager.getInstance().getCourseManager().getCourseByName(courseName);
            if (course == null) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Kurs o podanym ID nie istnieje.");
            } else {
                DatabaseManager.getInstance().getCourseManager().deleteCourse(courseName);
                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kurs został usunięty.");
            }
            System.out.println("Usunięto kurs o ID: " + courseName);
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowy format ID kursu.");
        }
    }

    private void openSelectedCourse() {
        Course selectedCourse = myCoursesTable.getSelectionModel().getSelectedItem();

        if (selectedCourse != null) {
            try {
                String link = selectedCourse.getLink();
                if (!link.startsWith("http://")&&!link.startsWith("https://")) {
                    link = "http://" + link;
                }

                java.awt.Desktop.getDesktop().browse(new URI(link));
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się otworzyć linku!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wybierz kurs przed otwarciem!");
        }
    }

    public void handleOpenCourseButtonClick() {
        openSelectedCourse();
    }
}