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

/**
 * Klasa DashBoardController zarządza logiką widoku panelu głównego aplikacji Speed Speak.
 *
 * <p>Odpowiada za obsługę kursów, użytkowników, transakcji oraz funkcji administracyjnych.
 * FXML jest używane do wiązania pól z interfejsem graficznym,
 * a większość funkcji bazuje na interakcji użytkownika z GUI.</p>
 *
 * <h3>Główne funkcjonalności:</h3>
 * <ul>
 *   <li>Zarządzanie listą kursów (zakup, filtrowanie, sortowanie, wyświetlanie).</li>
 *   <li>Paginacja oraz dynamiczne ładowanie popularnych kursów.</li>
 *   <li>Dodawanie i usuwanie kursów (dla administratorów).</li>
 *   <li>Zarządzanie użytkownikami, w tym zmiana hasła, usuwanie, przypisywanie kursów.</li>
 *   <li>Zarządzanie transakcjami, takie jak dodawanie środków do konta użytkownika.</li>
 *   <li>Wyświetlanie komunikatów w aplikacji za pomocą alertów.</li>
 * </ul>
 */
public class DashBoardController {

    // Pola FXML związane z układem interfejsu graficznego

    /** Kontenery dla różnych sekcji aplikacji (administracja, kursy, profil użytkownika itp.). */
    @FXML
    private VBox adminLayout, mainContainer, myCoursesContainer, adminContainer, courseContainer, transactionsContainer, courseVBox;

    /** Pola tekstowe do wprowadzania danych procesowanych w aplikacji. */
    @FXML
    private TextField amountField, searchField, courseNameField, courseDescriptionField, coursePriceField, courseLinkField;
    @FXML
    private TextField userEmailField, courseIdField, passwordField, deleteCourseIdField;

    /** Kontener wyświetlający kursy w sekcji "Najpopularniejsze kursy". */
    @FXML
    FlowPane coursePane;

    /** Kontrola paginacji dla wyświetlania kursów na stronie. */
    @FXML
    private Pagination pagination;

    /** Dropdown umożliwiający sortowanie kursów w sekcji "Kup kurs". */
    @FXML
    private ComboBox<String> sortComboBox;

    /** Etykiety wyświetlające dane użytkownika. */
    @FXML
    private Label usernameLabel, idKontaLabel, accountBalanceLabel;

    /** Obrazy używane w aplikacji (np. logo). */
    @FXML
    private ImageView logoImage, logoImage2;

    /** Przycisk do wylogowania oraz przycisk otwierania kursów. */
    @FXML
    private Button logoutButton, openCourseButton;

    /** Tabela kursów przypisanych użytkownikowi. */
    @FXML
    private TableView<Course> myCoursesTable;

    /** Kolumny tabeli kursów użytkownika (produkt, opis, cena). */
    @FXML
    private TableColumn<Course, String> productColumn, priceColumn, descriptionColumn;

    /**
     * Pole tekstowe, w którym wprowadzany jest adres e-mail użytkownika.
     *
     * <p>To pole jest wykorzystywane w sekcji administracyjnej, aby umożliwić
     * administratorowi weryfikację użytkownika na podstawie jego adresu e-mail.
     * Obsługiwane jest głównie przez metodę `handleCheckUserCourses`, która
     * zwraca listę kursów przypisanych do wprowadzonego użytkownika.</p>
     *
     * <h3>Zastosowanie:</h3>
     * <ul>
     *   <li>Sprawdzenie zakupionych kursów użytkownika (wraz z użyciem `handleCheckUserCourses`).</li>
     *   <li>Pomocnicze pole do weryfikacji użytkownika w funkcjach administracyjnych.</li>
     * </ul>
     *
     * @see #handleCheckUserCourses()
     */
    @FXML
    private TextField userEmailFieldStats;

    /**
     * Lista wyświetlająca kursy zakupione przez użytkownika.
     *
     * <p>To pole graficzne `ListView` wyświetla listę nazw kursów przypisanych
     * do użytkownika. Jest dynamicznie wypełniana na podstawie wyników zapytania
     * zwracanego przez metodę `handleCheckUserCourses`.</p>
     *
     * <h3>Funkcjonalność:</h3>
     * <ul>
     *   <li>Wyświetla listę nazw kursów przypisanych do użytkownika.</li>
     *   <li>Oczyszcza dane i ponownie wypełnia listę przy każdorazowym wywołaniu
     *       sprawdzenia kursów użytkownika.</li>
     * </ul>
     *
     * @see #handleCheckUserCourses()
     */
    @FXML
    private ListView<String> userPurchasedCoursesList;

    /**
     * Pole tekstowe do wprowadzania nowego salda użytkownika.
     *
     * <p>To pole jest używane w sekcji administracyjnej, aby administrator mógł zmienić
     * stan konta dowolnego użytkownika, wprowadzając nową wartość.
     * Wartość ta jest walidowana pod kątem poprawności formatowania liczby.</p>
     *
     * @see #updateUserBalance()
     */
    @FXML
    private TextField userBalanceField;

    /** Lista wszystkich kursów dostępnych w systemie. */
    private List<Course> allCourses;

    /** Lista kursów filtrowanych w sekcji "Kup kurs". */
    private ObservableList<Course> filteredCourseList;

    /** Liczba elementów wyświetlanych na jednej stronie w paginacji. */
    private static final int ITEMS_PER_PAGE = 5;

    /** Aktualnie zalogowany użytkownik. */
    private User currentUser;

    // =========================== FUNKCJE INICJALIZACYJNE ===========================

    /**
     * Inicjalizuje dane i konfigurację kontrolera po załadowaniu widoku przez JavaFX.
     * <p>Metoda ta odpowiada za przygotowanie danych użytkownika, listy kursów,
     * konfigurację widocznych elementów GUI oraz ustawienia tabel i paginacji.</p>
     *
     * @see #createAdminButton()
     * @see #setupPagination()
     */
    @FXML
    public void initialize() {
        // Ładowanie logo aplikacji
        logoImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));
        logoImage2.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png"))));

        // Pobieranie danych aktualnie zalogowanego użytkownika
        currentUser = DatabaseManager.getInstance().getUserManager()
                .getCurrentUserWithCourses(DatabaseManager.getInstance().getUserManager().getCurrentUser().getId());

        // Wyświetlenie danych użytkownika w GUI
        usernameLabel.setText("Witaj, " + currentUser.getUsername() + "!");
        idKontaLabel.setText("ID konta: " + currentUser.getId());
        accountBalanceLabel.setText("Stan konta: " + String.format("%.2f", currentUser.getBalance()) + " zł");

        // Dodanie przycisku Admin, jeśli aktualny użytkownik ma rolę administratora
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            createAdminButton();
        }

        // Inicjalizacja kursów i listy filtrowanej
        allCourses = DatabaseManager.getInstance().getCourseManager().getCourseList();
        filteredCourseList = FXCollections.observableArrayList(allCourses);

        // Konfiguracja dropdown dla sortowania kursów
        sortComboBox.getItems().addAll("Sortuj według ceny: od największej", "Sortuj według ceny: od najmniejszej");
        sortComboBox.setValue(sortComboBox.getItems().get(0));

        // Inicjalizacja paginacji
        setupPagination();

        // Konfiguracja kolumn tabeli kursów
        productColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        productColumn.setStyle("-fx-alignment: CENTER;");
        descriptionColumn.setStyle("-fx-alignment: CENTER;");
        priceColumn.setStyle("-fx-alignment: CENTER;");

        // Akcja powiązana z przyciskiem "Otwórz kurs"
        openCourseButton.setOnAction(event -> openSelectedCourse());
        populateRandomCoursesPane(allCourses);

    }

    /**
     * Tworzy przycisk "Admin" i dodaje go do widoku.
     * <p>Przycisk jest widoczny wyłącznie dla użytkownika, który posiada rolę administratora,
     * i służy do przejścia do sekcji panelu administracyjnego.</p>
     */
    private void createAdminButton() {
        Button adminButton = new Button("Admin");
        adminButton.setStyle("-fx-text-fill: white; -fx-background-color: #2e3b54; -fx-background-radius: 5;");
        adminButton.setOnMouseClicked(event -> toggleVisibility(false, false, false, false, true));
        adminLayout.getChildren().add(adminButton);
    }

    // =========================== ZARZĄDZANIE KURSAMI ===========================

    /**
     * Filtruje kursy na podstawie wprowadzonego tekstu oraz wybranej metody sortowania.
     * <p>Metoda aktualizuje listę wyświetlanych kursów na podstawie kryteriów użytkownika
     * wprowadzonych w polu wyszukiwania lub dropdown do sortowania.</p>
     *
     * @see #setupPagination()
     */
    @FXML
    private void filterCourses() {
        String searchText = searchField.getText().toLowerCase();
        String selectedSort = sortComboBox.getValue();

        // Filtrowanie kursów w zależności od tekstu wyszukiwania
        filteredCourseList.setAll(allCourses.stream()
                .filter(course -> course.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList()));

        // Sortowanie wyników według opcji wybranej przez użytkownika
        if (selectedSort != null) {
            if (selectedSort.contains("największej")) {
                filteredCourseList.sort((c1, c2) -> Double.compare(c2.getPrice(), c1.getPrice()));
            } else {
                filteredCourseList.sort(Comparator.comparingDouble(Course::getPrice));
            }
        }

        // Aktualizacja paginacji
        setupPagination();
    }

    /**
     * Konfiguruje paginację dla listy kursów.
     * <p>Paginacja dzieli listę kursów na mniejsze strony,
     * umożliwiając ich bardziej czytelne wyświetlanie.</p>
     */
    private void setupPagination() {
        pagination.setPageCount((int) Math.ceil((double) filteredCourseList.size() / ITEMS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);
        pagination.setPageFactory(this::createPage);
        showPage(0);
    }

    /**
     * Tworzy stronę kursów na podstawie paginacji.
     *
     * @param pageIndex indeks strony.
     * @return VBox z kursami przypisanymi do danej strony.
     */
    private VBox createPage(int pageIndex) {
        return showPage(pageIndex);
    }

    /**
     * Wyświetla kursy na określonej stronie.
     *
     * @param pageIndex indeks strony.
     * @return Kontener zawierający kursy na tej stronie.
     */
    private VBox showPage(int pageIndex) {
        courseVBox.getChildren().clear();
        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredCourseList.size());

        // Wyświetlanie kursów
        for (int i = start; i < end; i++) {
            createCourseButton(filteredCourseList.get(i));
        }

        pagination.setCurrentPageIndex(pageIndex);
        pagination.setPrefHeight(Integer.MAX_VALUE);
        return courseVBox;
    }

    /**
     * Wyświetla losowe popularne kursy w odpowiedniej sekcji widoku.
     *
     * <p>Metoda wybiera trzy losowe kursy i dynamicznie dodaje je
     * do sekcji "Najpopularniejsze kursy".</p>
     *
     * @param allCourses lista wszystkich kursów dostępnych w bazie danych.
     */
    public void populateRandomCoursesPane(List<Course> allCourses) {
        // Wyczyść istniejące kursy w sekcji
        coursePane.getChildren().clear();

        // Kopiowanie oryginalnej listy do manipulacji
        List<Course> remainingCourses = new ArrayList<>(allCourses);

        // Pobieranie maksymalnie 3 losowych kursów
        if (remainingCourses.isEmpty()) return;

        for (int i = 0; i < 3; i++) {
            if (remainingCourses.isEmpty()) break;

            // Losowy wybór kursu
            Random random = new Random();
            int randomIndex = random.nextInt(remainingCourses.size());
            Course pickedCourse = remainingCourses.remove(randomIndex);

            // Tworzenie wizualizacji kursu
            createPopularCourse(pickedCourse);
        }
    }

    /**
     * Tworzy graficzną reprezentację kursu dla sekcji popularnych kursów.
     *
     * @param course obiekt kursu do wyświetlenia.
     */
    private void createPopularCourse(Course course) {
        VBox coursesBox = new VBox(10);

        // Weryfikacja posiadania kursu przez użytkownika
        boolean hasCourse = currentUser.getCourses().stream().anyMatch(c -> c.getId().equals(course.getId()));

        // Styl kontenera w zależności od statusu kursu
        if (hasCourse) {
            coursesBox.setStyle("-fx-background-color: #7a7a7a; -fx-padding: 15px;");
        } else {
            coursesBox.setStyle("-fx-background-color: #2e3b54; -fx-padding: 15px;");
        }

        // Etykieta z nazwą kursu
        Label label = new Label(course.getName());
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Etykieta z opisem kursu
        Label description = new Label(course.getDescription());
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        // Przycisk z odpowiednią akcją (zakup lub informacja)
        Button button = new Button(hasCourse ? "Posiadasz kurs" : "Kup za " + course.getPrice() + " zł");
        button.setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50; -fx-background-radius: 5px; -fx-font-size: 14px;");
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER);

        if (hasCourse) {
            button.setDisable(true); // Jeśli użytkownik posiada kurs, przycisk jest wyłączony
        } else {
            button.setOnAction(event -> handleBuyCourse(course.getId())); // Zakup kursu
        }

        // Dodanie elementów do kontenera kursu
        coursesBox.getChildren().addAll(label, description, button);
        coursesBox.setAlignment(Pos.CENTER);

        // Dodanie kontenera kursu do sekcji popularnych kursów
        coursePane.getChildren().add(coursesBox);
    }

    /**
     * Obsługuje zakup kursu przez użytkownika.
     *
     * <p>Sprawdza, czy użytkownik posiada wystarczające środki na zakup kursu.
     * Po zakupie kursu aktualizuje saldo użytkownika oraz przypisuje kurs do jego profilu.</p>
     *
     * @param courseId ID kursu, który użytkownik chce zakupić.
     */
    private void handleBuyCourse(Long courseId) {
        Course course = DatabaseManager.getInstance().getCourseManager().getCourseById(courseId);

        // Sprawdzenie, czy użytkownik ma wystarczające środki na zakup
        if (currentUser.getBalance() < course.getPrice()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie masz wystarczających środków na koncie.");
            return;
        }

        // Przypisanie kursu do użytkownika
        currentUser.getCourses().add(course);
        currentUser.setBalance(currentUser.getBalance() - course.getPrice());

        // Aktualizacja danych użytkownika w bazie
        DatabaseManager.getInstance().getUserManager().updateUser(currentUser);

        // Aktualizacja stanu konta w interfejsie
        accountBalanceLabel.setText("Stan konta: " + String.format("%.2f", currentUser.getBalance()) + " zł");

        // Odświeżenie widoku kursów
        setupPagination();

        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zakupiono kurs: " + course.getName());
    }

    // ======================== OBSŁUGA SEKCJI PROFILE ========================

    /**
     * Obsługuje kliknięcie przycisku "Moje kursy".
     * Wyświetla kursy zakupione przez użytkownika w formie tabeli.
     */
    @FXML
    private void handleMyCoursesClick() {
        ObservableList<Course> courses = FXCollections.observableArrayList(currentUser.getCourses());

        myCoursesTable.setItems(courses);

        toggleVisibility(false, false, false, true, false);
    }
    /**
     * Sprawdza i wyświetla listę kursów zakupionych przez użytkownika.
     *
     * <p>Metoda pobiera adres e-mail użytkownika z pola tekstowego,
     * a następnie odczytuje listę kursów przypisanych do tego użytkownika.
     * Wyniki są wyświetlane w widoku listy kursów.</p>
     *
     * <h3>Działanie:</h3>
     * <ul>
     *   <li>Walidacja pola e-mail użytkownika.</li>
     *   <li>Pobranie użytkownika na podstawie podanego e-maila.</li>
     *   <li>Wyświetlenie listy zakupionych kursów w widoku ListView.</li>
     *   <li>Wyświetlenie alertu, jeśli użytkownik nie posiada kursów.</li>
     * </ul>
     */
    @FXML
    private void handleCheckUserCourses() {
        String userEmail = userEmailFieldStats.getText();

        if (userEmail == null || userEmail.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wprowadź poprawny adres e-mail użytkownika.");
            return;
        }

        User userWithCourses = DatabaseManager.getInstance().getUserManager().getUserWithCourses(userEmail);

        if (userWithCourses == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono użytkownika o podanym e-mailu.");
            return;
        }

        List<Course> purchasedCourses = userWithCourses.getCourses();

        if (purchasedCourses.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Informacja", "Użytkownik nie posiada zakupionych kursów.");
            return;
        }

        userPurchasedCoursesList.getItems().clear();
        userPurchasedCoursesList.getItems().addAll(purchasedCourses.stream().map(Course::getName).collect(Collectors.toList()));
    }
    /**
     * Aktualizuje saldo użytkownika na podstawie wprowadzonego przez administratora balansu.
     *
     * <p>Metoda pobiera adres e-mail użytkownika oraz nową kwotę salda wprowadzone przez administratora.
     * Po przeprowadzeniu walidacji i sprawdzeniu poprawności danych aktualizuje saldo użytkownika w systemie.</p>
     *
     * <h3>Działanie:</h3>
     * <ul>
     *   <li>Weryfikacja, czy pola e-mail i saldo nie są puste.</li>
     *   <li>Parsowanie kwoty i walidacja poprawności liczbowej.</li>
     *   <li>Pobranie użytkownika z bazy i aktualizacja salda w systemie.</li>
     *   <li>Wyświetlenie stosownego komunikatu w przypadku błędu lub sukcesu operacji.</li>
     * </ul>
     */
    @FXML
    private void updateUserBalance() {
        try {
            String userEmail = userEmailField.getText();
            String balanceText = userBalanceField.getText();

            // Walidacja pól
            if (userEmail == null || userEmail.isEmpty() || balanceText == null || balanceText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Proszę podać poprawny e-mail i balans.");
                return;
            }

            double newBalance;
            try {
                newBalance = Double.parseDouble(balanceText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Proszę podać poprawny format liczby dla balansu.");
                return;
            }

            // Pobranie użytkownika z bazy
            User user = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono użytkownika o podanym e-mailu.");
                return;
            }

            // Aktualizacja balansu użytkownika
            user.setBalance(newBalance);
            DatabaseManager.getInstance().getUserManager().updateUser(user);

            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Balans użytkownika został zaktualizowany.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd podczas aktualizacji balansu użytkownika.");
        }
    }

    /**
     * Tworzy przycisk odpowiadający za wizualną reprezentację kursu na stronie kursów.
     *
     * <p>Metoda generuje dynamicznie element GUI reprezentujący kurs. Dla każdego kursu jest tworzony
     * kontener zawierający nazwę kursu, opis oraz przycisk "Kup", który umożliwia zakup kursu,
     * jeśli użytkownik jeszcze go nie posiada.</p>
     *
     * <h3>Działanie:</h3>
     * <ul>
     *   <li>Tworzy kontener typu `VBox` dla wizualizacji kursu.</li>
     *   <li>Dodaje etykiety (nazwa, opis) oraz przycisk (np. "Kup za X zł").</li>
     *   <li>Jeśli użytkownik posiada kurs, przycisk jest dezaktywowany.</li>
     *   <li>Dodaje dynamicznie wygenerowany kontener kursu do sekcji `courseVBox`.</li>
     * </ul>
     *
     * @param course obiekt kursu, który ma być dodany do widoku.
     */
    private void createCourseButton(Course course) {
        VBox coursesBox = new VBox(10);

        // Sprawdzenie posiadania kursu przez użytkownika
        boolean hasCourse = currentUser.getCourses().stream().anyMatch(c -> c.getId().equals(course.getId()));

        // Ustawienie stylu kontenera
        if (hasCourse) {
            coursesBox.setStyle("-fx-background-color: #7a7a7a; -fx-padding: 15px;");
        } else {
            coursesBox.setStyle("-fx-background-color: #2e3b54; -fx-padding: 15px;");
        }

        // Etykieta z nazwą kursu
        Label label = new Label(course.getName());
        label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Etykieta z opisem kursu
        Label description = new Label(course.getDescription());
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        // Przycisk zakupu
        Button button = new Button(hasCourse ? "Posiadasz kurs" : "Kup za " + course.getPrice() + " zł");
        button.setStyle("-fx-text-fill: white; -fx-background-color: #4CAF50; -fx-background-radius: 5px; -fx-font-size: 14px;");
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER);

        if (hasCourse) {
            button.setDisable(true);
        } else {
            button.setOnAction(event -> handleBuyCourse(course.getId()));
        }

        // Dodanie elementów do kontenera kursu
        coursesBox.getChildren().addAll(label, description, button);
        coursesBox.setAlignment(Pos.CENTER);

        // Dodanie do globalnego kontenera
        courseVBox.getChildren().add(coursesBox);

    }

    /**
     * Otwiera link wybranego kursu w przeglądarce internetowej użytkownika.
     *
     * <p>Metoda sprawdza, czy wybrano kurs w tabeli "Moje kursy". Jeśli kurs został wybrany,
     * próbuje otworzyć pole `link` kursu w domyślnej przeglądarce internetowej systemu użytkownika.</p>
     *
     * <p>Jeżeli link nie zaczyna się od "http://" lub "https://", metoda automatycznie dodaje prefix "http://".</p>
     *
     * <h3>Działanie:</h3>
     * <ul>
     *   <li>Weryfikuje, czy wybrano kurs w tabeli.</li>
     *   <li>Próbuje otworzyć link kursu w przeglądarce systemowej.</li>
     *   <li>Wyświetla alert w przypadku braku kursu lub błędu otwarcia linku.</li>
     * </ul>
     */
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

    /**
     * Obsługuje kliknięcie przycisku "Otwórz kurs".
     *
     * <p>Metoda przekazuje kontrolę do metody <code>openSelectedCourse</code>,
     * otwierającej link kursu wybranego w tabeli "Moje kursy".</p>
     *
     * @see #openSelectedCourse()
     */
    public void handleOpenCourseButtonClick() {
        openSelectedCourse();
    }
    /**
     * Przełącza widoczność odpowiednich sekcji w panelu głównym.
     *
     * @param showCourses     widoczność sekcji "Kup kurs".
     * @param showMain        widoczność głównego dashboardu.
     * @param showTransactions widoczność sekcji "Dodaj środki".
     * @param showMyCourses   widoczność sekcji "Moje kursy".
     * @param showAdmin       widoczność panelu administracyjnego.
     */
    private void toggleVisibility(boolean showCourses, boolean showMain, boolean showTransactions, boolean showMyCourses, boolean showAdmin) {
        courseContainer.setVisible(showCourses);
        mainContainer.setVisible(showMain);
        transactionsContainer.setVisible(showTransactions);
        myCoursesContainer.setVisible(showMyCourses);
        adminContainer.setVisible(showAdmin);
    }

    // ======================== OBSŁUGA ADMINISTRATORA ========================

    /**
     * Obsługuje dodawanie nowego kursu do systemu.
     *
     * <p>Dodaje kurs do bazy danych na podstawie danych wprowadzonych przez administratora.</p>
     */
    @FXML
    private void handleAddCourse() {
        try {
            String name = courseNameField.getText();
            String description = courseDescriptionField.getText();
            double price = Double.parseDouble(coursePriceField.getText());
            String link = courseLinkField.getText();

            if (name.isEmpty() || description.isEmpty() || link.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Wszystkie pola muszą być wypełnione.");
                return;
            }

            if (name.length() < 3) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Nazwa musi miec wiecej niz 3 znaki.");
                return;
            }

            // Dodanie kursu do bazy danych
            Course course = new Course();
            course.setName(name);
            course.setDescription(description);
            course.setPrice(price);
            course.setLink(link);

            DatabaseManager.getInstance().getCourseManager().addCourse(course);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Dodano nowy kurs: " + name);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Cena kursu musi być liczbą.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas dodawania kursu.");
        }
    }

    /**
     * Obsługuje usunięcie kursu z systemu.
     *
     * @see DatabaseManager#getCourseManager()
     */
    @FXML
    private void handleDeleteCourse() {
        try {
            String courseName = deleteCourseIdField.getText();
            Course course = DatabaseManager.getInstance().getCourseManager().getCourseByName(courseName);

            if (course == null) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Kurs o nazwie " + courseName + " nie istnieje.");
            } else {
                DatabaseManager.getInstance().getCourseManager().deleteCourse(courseName);
                showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kurs został usunięty.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć kursu.");
        }
    }


// ======================== OBSŁUGA UŻYTKOWNIKÓW ========================

    /**
     * Obsługuje przypisanie kursu do użytkownika.
     *
     * <p>Na podstawie podanego adresu e-mail użytkownika i nazwy kursu,
     * metoda przypisuje kurs do użytkownika w bazie danych.</p>
     */
    @FXML
    private void handleAssignCourse() {
        String userEmail = userEmailField.getText();
        String courseName = courseIdField.getText();

        // Pobranie użytkownika na podstawie e-maila
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        // Przypisanie kursu użytkownikowi
        DatabaseManager.getInstance().getUserManager().handleAssignCourse(userByEmail, courseName);
        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Przypisano kurs " + courseName + " użytkownikowi o e-mailu " + userEmail);
    }

    /**
     * Obsługuje usunięcie przypisanego kursu od użytkownika.
     *
     * <p>Na podstawie podanego adresu e-mail i nazwy kursu usuwa przypisanie kursu
     * od użytkownika w bazie danych.</p>
     */
    @FXML
    private void handleRemoveCourse() {
        String userEmail = userEmailField.getText();
        String courseName = courseIdField.getText();

        // Pobranie użytkownika na podstawie e-maila
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        // Usunięcie przypisanego kursu
        DatabaseManager.getInstance().getUserManager().handleRemoveCourse(userByEmail, courseName);
        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Usunięto kurs " + courseName + " z użytkownika o e-mailu " + userEmail);
    }

    /**
     * Wyświetla okno głównego panelu aplikacji.
     *
     * <p>Metoda inicjalizuje widok i prezentuje okno główne aplikacji
     * przy użyciu FXML. Ustawia minimalne wymiary okna, nazwę tytułu,
     * a także wyśrodkowuje okno na ekranie użytkownika.</p>
     *
     * <h3>Działanie:</h3>
     * <ul>
     *   <li>Ładuje plik FXML widoku panelu głównego (<code>Dashboard.fxml</code>).</li>
     *   <li>Ustawia tytuł okna: "Panel główny".</li>
     *   <li>Określa minimalne rozmiary okna: szerokość 930 px, wysokość 1000 px.</li>
     *   <li>Tworzy scenę i wyświetla okno wyśrodkowane na ekranie.</li>
     * </ul>
     *
     * @param stage instancja obiektu `Stage`, na której ma zostać załadowany widok.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML.
     */
    public void showWindow(Stage stage) throws IOException {



        // Ładowanie widoku z pliku Dashboard.fxml
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));

        // Ustawienie tytułu okna
        stage.setTitle("Panel główny");

        // Definiowanie minimalnych wymiarów okna
        stage.setMinHeight(1000);
        stage.setMinWidth(930);

        // Ustawienie sceny z widokiem
        stage.setScene(new Scene(parent));

        // Wyświetlenie okna i wyśrodkowanie go na ekranie użytkownika
        stage.show();
        stage.centerOnScreen();
    }

    /**
     * Obsługuje usuwanie użytkownika z bazy danych.
     *
     * <p>Na podstawie podanego adresu e-mail, metoda usuwa użytkownika z bazy danych.
     * Weryfikuje, czy użytkownik istnieje i zabrania usuwania samego administratora.</p>
     */
    @FXML
    private void handleDeleteUser() {
        String userEmail = userEmailField.getText();

        // Pobranie użytkownika z bazy
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        } else if (userByEmail.getEmail().equals(currentUser.getEmail())) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie możesz usunąć siebie.");
            return;
        }

        // Usuwanie użytkownika
        DatabaseManager.getInstance().getUserManager().deleteUser(userByEmail);
        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik " + userEmail + " został usunięty.");
    }

    /**
     * Obsługuje resetowanie hasła użytkownika.
     *
     * <p>Wykorzystuje podany adres e-mail, aby zidentyfikować użytkownika
     * i umożliwić aktualizację jego hasła w bazie danych.</p>
     */
    @FXML
    private void handleResetPassword() {
        String userEmail = userEmailField.getText();
        String newPassword = passwordField.getText();

        // Pobranie użytkownika z bazy
        User userByEmail = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

        if (userByEmail == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        // Resetowanie hasła użytkownika
        DatabaseManager.getInstance().getUserManager().updatePassword(userEmail, userByEmail.getPassword(), newPassword);
        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Hasło użytkownika zostało zaktualizowane.");
    }

    // ======================== OBSŁUGA TRANSAKCJI ========================

    /**
     * Obsługuje proces doładowania konta użytkownika.
     *
     * <p>Pobiera kwotę wprowadzoną przez użytkownika, weryfikuje jej poprawność
     * i dodaje ją do aktualnego salda konta użytkownika w bazie danych.</p>
     */
    @FXML
    public void handleAddFunds() {
        processFunds(amountField.getText());
    }

    /**
     * Przetwarza wprowadzoną kwotę w celu doładowania konta użytkownika.
     *
     * <p>Waliduje poprawność kwoty (czy jest liczbową wartością większą od zera),
     * a następnie aktualizuje saldo użytkownika w bazie danych.</p>
     *
     * @param amountInput wprowadzona kwota doładowania.
     */
    private void processFunds(String amountInput) {
        try {
            double amount = Double.parseDouble(amountInput);

            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Kwota musi być większa od zera.");
                return;
            }

            // Aktualizacja konta użytkownika
            currentUser.setBalance(currentUser.getBalance() + amount);
            DatabaseManager.getInstance().getUserManager().updateUser(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Doładowano konto o " + amount + " zł.");

            amountField.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawna liczba. Wprowadź liczbę.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd podczas doładowania konta.");
            e.printStackTrace();
        }
    }

    // ======================== OBSŁUGA WIDOKÓW ========================

    /**
     * Obsługuje przejście do sekcji "Kup kurs".
     *
     * <p>Ustawia widoczność panelu "Kup kurs" i aktualizuje stan konta użytkownika
     * wyświetlany w interfejsie.</p>
     */
    @FXML
    public void handleBuyCourseClick() {
        accountBalanceLabel.setText("Stan konta: " + String.format("%.2f", currentUser.getBalance()) + " zł");
        toggleVisibility(true, false, false, false, false);
    }

    /**
     * Obsługuje przejście do sekcji "Historia transakcji".
     */
    public void handleTransactionsClick() {
        toggleVisibility(false, false, true, false, false);
    }

    /**
     * Obsługuje powrót do głównego widoku panelu głównego.
     */
    public void handleHomeClick() {
        toggleVisibility(false, true, false, false, false);
        populateRandomCoursesPane(allCourses);
    }

    /**
     * Wylogowuje użytkownika z systemu i przełącza widok na ekran logowania.
     */
    public void handleLogout() {
        DatabaseManager.getInstance().getUserManager().setCurrentUser(null);
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        try {
            ControllerManager.getInstance().getLoginController().showWindow(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wyświetla alert z komunikatem o podanym typie.
     *
     * @param alertType typ alertu (np. informacja, błąd).
     * @param title     tytuł alertu.
     * @param message   treść komunikatu do wyświetlenia.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}