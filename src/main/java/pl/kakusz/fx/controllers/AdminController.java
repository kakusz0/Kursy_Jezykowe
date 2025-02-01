package pl.kakusz.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import pl.kakusz.database.managers.DatabaseManager;
import pl.kakusz.database.objects.Course;
import pl.kakusz.database.objects.User;
import pl.kakusz.fx.AlertHelper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {
    @FXML
    private TextField amountField, searchField, courseNameField, courseDescriptionField, coursePriceField, courseLinkField;
    @FXML
    private TextField userEmailField, courseIdField, passwordField, deleteCourseIdField;

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
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Wszystkie pola muszą być wypełnione.");
                return;
            }

            if (name.length() < 3) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nazwa musi miec wiecej niz 3 znaki.");
                return;
            }

            Course courseData = DatabaseManager.getInstance().getCourseManager().getCourseByName(name);

            if (courseData != null) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Kurs o tej nazwie juz istnieje.");
                return;
            }

            if(price < 0) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Cena musi być dodatnia.");
                return;
            }

            if(link.length() < 3) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Link musi miec wiecej niz 2 znaki.");
                return;
            }

            if(description.length() < 3) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Opis musi miec wiecej niz 2 znaki.");
                return;
            }


            // Dodanie kursu do bazy danych
            Course course = new Course();
            course.setName(name);
            course.setDescription(description);
            course.setPrice(price);
            course.setLink(link);

            DatabaseManager.getInstance().getCourseManager().addCourse(course);
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Dodano nowy kurs: " + name);
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Cena kursu musi być liczbą.");
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił problem podczas dodawania kursu.");
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
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Kurs o nazwie " + courseName + " nie istnieje.");
            } else {
                DatabaseManager.getInstance().getCourseManager().deleteCourse(courseName);
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Kurs został usunięty.");
            }
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć kursu.");
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
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        // Przypisanie kursu użytkownikowi
        DatabaseManager.getInstance().getUserManager().handleAssignCourse(userByEmail, courseName);
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Przypisano kurs " + courseName + " użytkownikowi o e-mailu " + userEmail);
    }
    @FXML
    private TextField userEmailFieldStats;


    @FXML
    private ListView<String> userPurchasedCoursesList;

    @FXML
    private void handleCheckUserCourses() {
        String userEmail = userEmailFieldStats.getText();

        if (userEmail == null || userEmail.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Wprowadź poprawny adres e-mail użytkownika.");
            return;
        }

        User userWithCourses = DatabaseManager.getInstance().getUserManager().getUserWithCourses(userEmail);

        if (userWithCourses == null) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono użytkownika o podanym e-mailu.");
            return;
        }

        List<Course> purchasedCourses = userWithCourses.getCourses();

        if (purchasedCourses.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Informacja", "Użytkownik nie posiada zakupionych kursów.");
            return;
        }

        userPurchasedCoursesList.getItems().clear();
        userPurchasedCoursesList.getItems().addAll(purchasedCourses.stream().map(Course::getName).collect(Collectors.toList()));
    }

    @FXML
    private TextField userBalanceField;

    @FXML
    private void checkUserBalance() {
        try {
            String userEmail = userEmailField.getText();

            // Walidacja pola e-mail
            if (userEmail == null || userEmail.isEmpty()) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Proszę podać poprawny e-mail.");
                return;
            }

            // Pobranie użytkownika z bazy
            User user = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

            if (user == null) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono użytkownika o podanym e-mailu.");
                return;
            }

            // Wyświetlenie aktualnego balansu użytkownika
            double currentBalance = user.getBalance();
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Balans Użytkownika",
                    "Aktualny balans użytkownika: " + currentBalance);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd podczas sprawdzania balansu użytkownika.");
        }
    }

    @FXML
    private void updateUserBalance() {
        try {
            String userEmail = userEmailField.getText();
            String balanceText = userBalanceField.getText();

            // Walidacja pól
            if (userEmail == null || userEmail.isEmpty() || balanceText == null || balanceText.isEmpty()) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Proszę podać poprawny e-mail i balans.");
                return;
            }

            double newBalance;
            try {
                newBalance = Double.parseDouble(balanceText);
            } catch (NumberFormatException e) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Proszę podać poprawny format liczby dla balansu.");
                return;
            }

            // Pobranie użytkownika z bazy
            User user = DatabaseManager.getInstance().getUserManager().getUserByEmail(userEmail);

            if (user == null) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono użytkownika o podanym e-mailu.");
                return;
            }

            // Aktualizacja balansu użytkownika
            user.setBalance(newBalance);
            DatabaseManager.getInstance().getUserManager().updateUser(user);

            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Balans użytkownika został zaktualizowany.");
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd podczas aktualizacji balansu użytkownika.");
        }
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
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        // Resetowanie hasła użytkownika
        DatabaseManager.getInstance().getUserManager().updatePassword(userEmail, userByEmail.getPassword(), newPassword);
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Hasło użytkownika zostało zaktualizowane.");
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
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        } else if (userByEmail.getEmail().equals(DatabaseManager.getInstance().getUserManager().getCurrentUser().getEmail())) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Nie możesz usunąć siebie.");
            return;
        }

        // Usuwanie użytkownika
        DatabaseManager.getInstance().getUserManager().deleteUser(userByEmail);
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Użytkownik " + userEmail + " został usunięty.");
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
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Błąd", "Użytkownik o podanym adresie e-mail nie istnieje.");
            return;
        }

        // Usunięcie przypisanego kursu
        DatabaseManager.getInstance().getUserManager().handleRemoveCourse(userByEmail, courseName);
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Sukces", "Usunięto kurs " + courseName + " z użytkownika o e-mailu " + userEmail);
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
}
