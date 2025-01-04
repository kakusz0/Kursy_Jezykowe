package pl.kakusz.fx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import pl.kakusz.fx.ControllerManager;

import java.io.IOException;

public class PrivacyPolicyController {

    @FXML
    private Button backToRegisterButton;

    @FXML
    private void handleBackToRegister() throws IOException {
        ControllerManager.getInstance().getRegisterController().showWindow((Stage) backToRegisterButton.getScene().getWindow());
    }
    @FXML
    private TextArea privacyPolicyTextArea;

    @FXML
    public void initialize() {
        String privacyPolicyText = "1. Wstęp\nSzanujemy Twoją prywatność i chronimy dane osobowe. Polityka Prywatności wyjaśnia, jakie dane zbieramy i jak je wykorzystujemy.\n\n" +
                "2. Zbierane dane\nZbieramy dane osobowe, takie jak imię, nazwisko, adres e-mail, dane konta użytkownika oraz dane o aktywności w Aplikacji.\n\n" +
                "3. Cel zbierania danych\nDane są zbierane w celu świadczenia usług, poprawy jakości aplikacji oraz komunikacji z użytkownikiem.\n\n" +
                "4. Twoje prawa\nMasz prawo do dostępu, sprostowania, usunięcia lub przeniesienia swoich danych.\n\n" +
                "5. Kontakt\nW razie pytań lub wniosków prosimy o kontakt pod adresem support@speedspeak.pl";
        //privacyPolicyTextArea.setMouseTransparent(true);
        privacyPolicyTextArea.setText(privacyPolicyText);
    }

    public void showWindow(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/PrivacyPolicy.fxml"));
        Parent root = loader.load();
        stage.setTitle("Polityka Prywatności");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
