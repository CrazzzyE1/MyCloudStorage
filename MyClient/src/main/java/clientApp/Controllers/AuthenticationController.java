package clientApp.Controllers;

import clientApp.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class AuthenticationController {
    @FXML
    Button authButton;
    @FXML
    TextField login;
    @FXML
    TextField password;
    @FXML
    Hyperlink regLink;

    private Client client;

    public AuthenticationController() {
        client = Client.getInstance();
    }

    public void auth() {
        String msg = "auth ".concat(login.getText()).concat(" ").concat(password.getText());
        client.sendMessage(msg);
        msg = client.readMessage();
        System.out.println(msg);
        if (msg.equals("authsuccess")) {
            changeWindow("cloud");
        }
    }

    public void changeWindow(String fxmlName) {
        try {
            String fxml = "/fxml/" + fxmlName + ".fxml";
            Parent chat = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(fxmlName);
            stage.getIcons().add(new Image("/img/icon2.png"));
            stage.setScene(new Scene(chat));
            stage.setResizable(false);
            stage.show();
            login.getScene().getWindow().hide();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void registration() {
        changeWindow("registration");
    }
}