package ro.ubbcluj.map.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.ValidationException;
import ro.ubbcluj.map.service.UsersService;

public class EditUserController implements Controller{
    @FXML
    private TextField textFieldFirstName;
    @FXML
    private TextField textFieldLastName;
    @FXML
    private TextField textFieldId;

    private Stage dialogStage;
    private UsersService usersService;
    private Utilizator user;
    private EditType type;


    @FXML
    private void initialize(){}
    public void setService(UsersService usersService, Stage dialogStage, Utilizator u){
        this.usersService = usersService;
        this.dialogStage = dialogStage;
        this.user = u;
        type = EditType.SAVE;
        textFieldId.setEditable(false);
        if(u != null){
            setFields();
            type = EditType.UPDATE;
        }
    }

    private void setFields() {
        textFieldId.setText(user.getId());
        textFieldFirstName.setText(user.getFirstName());
        textFieldLastName.setText(user.getLastName());
    }

    @FXML
    public void handleSave(){
        String id = textFieldId.getText();
        String firstName = textFieldFirstName.getText();
        String lastName = textFieldLastName.getText();
        Utilizator u = new Utilizator(firstName, lastName, id, user.getPassword());
        u.setId(id);
        if(type == EditType.SAVE)
            saveUser(u);
        else if (type == EditType.UPDATE) {
            updateUser(u);
        }
    }

    private void updateUser(Utilizator u) {
        try {
            Utilizator user = usersService.update(u);
            if(user == null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Actualizare utilizator", "Utilizator actualizat cu succes!");
            else
                MessageAlert.showErrorMessage(null, "Utilizator inexistent!");
        } catch (ValidationException e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
        this.dialogStage.close();
    }

    private void saveUser(Utilizator u) {
        try {
            Utilizator user = usersService.add(u);
            if(user == null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Salvare utilizator", "Utilizator salvat cu succes!");
            else
                MessageAlert.showErrorMessage(null, "Utilizator deja existent!");
        } catch(ValidationException e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
        this.dialogStage.close();
    }

    @FXML
    public void handleCancel(){this.dialogStage.close();}

    @Override
    public void close() {
        this.dialogStage.close();
    }
}
