package ro.ubbcluj.map.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ro.ubbcluj.map.domain.entities.Message;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.observer.Observer;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class ChatController implements Observer<UtilizatorChangeEvent>, Controller {
    @FXML
    private Label labelReply;
    @FXML
    private TextField textFieldMessage;
    @FXML
    private ListView<Message> listView;

    @FXML
    private final ContextMenu contextMenu = new ContextMenu();

    @FXML
    private final MenuItem menuItemReply = new MenuItem("Reply");

    private Stage dialogStage;
    private UsersService usersService;
    private Utilizator u1;
    private Utilizator u2;
    private final ObservableList<Message> model = FXCollections.observableArrayList();

    public void setService(UsersService usersService, Stage dialogStage, Utilizator u1, Utilizator u2){
        this.usersService = usersService;
        this.dialogStage = dialogStage;
        this.u1 = u1;
        this.u2 = u2;
        usersService.addObserver(this);
        initModel();
    }

    @FXML
    private void initialize(){
        listView.setItems(model);
        listView.setContextMenu(contextMenu);
    }
    private void initModel() {
        List<Message> messages = StreamSupport.stream(usersService.getMessages(u1, u2).spliterator(), false).toList();
        model.setAll(messages);
    }

    public void handleSend(ActionEvent actionEvent) {
        if(textFieldMessage.getText().isEmpty()){
            MessageAlert.showErrorMessage(null, "Mesaj invalid!");
            return;
        }
        if(labelReply.getText().isEmpty())
            usersService.sendMessage(u1, Collections.singletonList(u2), textFieldMessage.getText().strip());
        else{
            usersService.replyMessage(u1, u2, listView.getSelectionModel().getSelectedItem(), textFieldMessage.getText().strip());
            labelReply.setText("");
        }
        textFieldMessage.clear();
    }

    @Override
    public void update(UtilizatorChangeEvent utilizatorChangeEvent) {
        initModel();
    }

    public void onContextMenuReq() {
        Message message = listView.getSelectionModel().getSelectedItem();
        menuItemReply.setOnAction(event -> labelReply.setText("reply to \"" + message.getContent() + "\":"));
        contextMenu.getItems().setAll(menuItemReply);
    }

    public void handleExit(ActionEvent actionEvent) {
        this.dialogStage.close();
    }

    public void setOnKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (textFieldMessage.getText().isBlank()) {
                MessageAlert.showErrorMessage(null, "Mesaj invalid!");
                return;
            }
            if(labelReply.getText().isEmpty())
                usersService.sendMessage(u1, Collections.singletonList(u2), textFieldMessage.getText().strip());
            else{
                usersService.replyMessage(u1, u2, listView.getSelectionModel().getSelectedItem(), textFieldMessage.getText().strip());
                labelReply.setText("");
            }
            textFieldMessage.clear();
        }
    }

    @Override
    public void close() {
        this.dialogStage.close();
    }
}
