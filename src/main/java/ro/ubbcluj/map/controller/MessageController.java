package ro.ubbcluj.map.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.service.UsersService;

import java.util.List;

public class MessageController{
    @FXML
    private TextArea textAreaMessage;

    private UsersService usersService;
    private Stage dialogStage;
    private Utilizator from;
    private List<Utilizator> to;
    public void setService(UsersService usersService, Stage dialogStage, Utilizator from, List<Utilizator> to) {
        this.from = from;
        this.dialogStage = dialogStage;
        this.usersService = usersService;
        this.to = to;
    }

    public void handleSend(){
        if(textAreaMessage.getText().isEmpty()){
            MessageAlert.showErrorMessage(null, "Mesaj invalid!");
            return;
        }
        usersService.sendMessage(from, to, textAreaMessage.getText().strip());
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Succes!", "Mesaj trimis cu succes!");
        dialogStage.close();
    }

    public void setOnKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (textAreaMessage.getText().isBlank()) {
                MessageAlert.showErrorMessage(null, "Mesaj invalid!");
                return;
            }
            usersService.sendMessage(from, to, textAreaMessage.getText().strip());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Succes!", "Mesaj trimis cu succes!");
            dialogStage.close();
        }
    }
}
