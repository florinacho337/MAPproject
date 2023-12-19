package ro.ubbcluj.map.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.UserApplication;
import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.entities.dtos.FriendRequestDTO;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.exceptions.DuplicateException;
import ro.ubbcluj.map.utils.observer.Observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class AdminController implements Observer<UtilizatorChangeEvent> {

    private UsersService usersService;
    private FriendshipsService friendshipsService;
    private final ObservableList<Utilizator> modelUsers = FXCollections.observableArrayList();
    private final ObservableList<FriendRequestDTO> modelFR = FXCollections.observableArrayList();
    @FXML
    private TableView<Utilizator> tableViewUsers;
    @FXML
    private TableColumn<Utilizator, String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator, String> tableColumnLastName;
    @FXML
    private TableColumn<Utilizator, String> tableColumnId;
    @FXML
    private TableColumn<FriendRequestDTO, String> tableColumnStatus;
    @FXML
    private TableColumn<FriendRequestDTO, Long> tableColumnFrom;
    @FXML
    private TableColumn<FriendRequestDTO, Long> tableColumnTo;
    @FXML
    private TableView<FriendRequestDTO> tableViewFriendRequest;

    @FXML
    private TextField textFieldSetUser;

    private Stage stage;
    private List<Stage> dialogs;

    public void setUsersService(UsersService usersService, FriendshipsService friendshipsService, Stage stage){
        this.friendshipsService = friendshipsService;
        this.usersService = usersService;
        this.stage = stage;
        dialogs = new ArrayList<>();
        usersService.addObserver(this);
        friendshipsService.addObserver(this);
        initModel();
        initStage();
    }

    private void initStage(){
        stage.setOnCloseRequest(event -> dialogs.forEach(Stage::close));
    }
    @FXML
    public void initialize() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableColumnFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        tableColumnTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        tableViewUsers.setItems(modelUsers);
        tableViewUsers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewFriendRequest.setItems(modelFR);
        textFieldSetUser.textProperty().addListener(o -> initModel());
    }
    private void initModel() {
        List<Utilizator> usersList = StreamSupport.stream(usersService.getAll().spliterator(), false).filter(utilizator -> textFieldSetUser.getText().isEmpty() ||
                !(Objects.equals(utilizator.getId(), textFieldSetUser.getText()))).toList();
        modelUsers.setAll(usersList);
        Iterable<FriendRequest> friendRequests = friendshipsService.getFriendRequests();
        List<FriendRequestDTO> friendRequestDTOList = StreamSupport.stream(friendRequests.spliterator(), false)
                .map(FriendRequestDTO::new)
                .filter(friendRequestDTO -> !textFieldSetUser.getText().isEmpty() &&
                        (Objects.equals(friendRequestDTO.getFrom(), textFieldSetUser.getText()) ||
                                Objects.equals(friendRequestDTO.getTo(), textFieldSetUser.getText())))
                .toList();
        modelFR.setAll(friendRequestDTOList);
    }

    @Override
    public void update(UtilizatorChangeEvent e) {
        initModel();
    }

    public void onAddButtonClick(ActionEvent actionEvent) {
        showEditUserDialog(null);
    }

    public void onUpdateButtonClick(ActionEvent actionEvent) {
        Utilizator user = tableViewUsers.getSelectionModel().getSelectedItem();
        if(user != null)
            showEditUserDialog(user);
        else
            MessageAlert.showErrorMessage(null, "Nu ati selectat nici un utilizator!");
    }

    private void showEditUserDialog(Utilizator user) {
        try {
            FXMLLoader loader = new FXMLLoader(UserApplication.class.getResource("edituser-view.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            EditUserController controller = loader.getController();
            controller.setService(usersService, dialogStage, user);
            dialogs.add(dialogStage);

            dialogStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void onDeleteButtonClick(ActionEvent actionEvent){
        Utilizator selected = tableViewUsers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Utilizator deleted = usersService.remove(selected.getId());
            if (null != deleted)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Delete", "Utilizatorul a fost sters cu succes!");
        } else MessageAlert.showErrorMessage(null, "Nu ati selectat nici un utilizator!");
    }

    public void handleExit(ActionEvent actionEvent) {
        this.dialogs.forEach(Stage::close);
        this.stage.close();
    }

    public void handleSendFR(ActionEvent actionEvent) {
        try {

            if (Objects.equals(textFieldSetUser.getText(), ""))
                MessageAlert.showErrorMessage(null, "Nu ati selectat utilizatorul curent!");
            Utilizator to = tableViewUsers.getSelectionModel().getSelectedItem();
            Utilizator from = usersService.find(textFieldSetUser.getText());
            if (friendshipsService.sendFriendRequest(from, to) == null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Friend Request", "Cerere de prietenie trimisa cu succes!");
            else
                MessageAlert.showErrorMessage(null, "Cerere de prietenie existenta!");
        } catch (DuplicateException e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleAccept(ActionEvent actionEvent) {
        try {
            FriendRequestDTO friendRequestDTO = tableViewFriendRequest.getSelectionModel().getSelectedItem();
            if(friendRequestDTO == null) {
                MessageAlert.showErrorMessage(null, "Nu ati selectat nici o cerere de prietenie!");
                return;
            }
            FriendRequest friendRequest = friendRequestDTO.getFriendRequest();
            if (Objects.equals(friendRequest.getStatus(), "approved") || Objects.equals(friendRequest.getStatus(), "rejected"))
                MessageAlert.showErrorMessage(null, "Cererea de prietenie a fost deja acceptata sau refuzata!");
            else {
                friendRequest = friendshipsService.acceptFriendRequest(friendRequest);
                if (friendRequest == null)
                    MessageAlert.showErrorMessage(null, "Cerere de prietenie inexistenta!");
                else
                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Accept", "Cerere de prietenie acceptata!");
            }
        }catch (DuplicateException e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleReject(ActionEvent actionEvent) {
        FriendRequestDTO friendRequestDTO = tableViewFriendRequest.getSelectionModel().getSelectedItem();
        if(friendRequestDTO == null){
            MessageAlert.showErrorMessage(null, "Nu ati selectat nici o cerere de prietenie!");
            return;
        }
        FriendRequest friendRequest = friendRequestDTO.getFriendRequest();
        if(Objects.equals(friendRequest.getStatus(), "approved") || Objects.equals(friendRequest.getStatus(), "rejected"))
            MessageAlert.showErrorMessage(null, "Cererea de prietenie a fost deja acceptata sau refuzata!");
        else {
            friendRequest = friendshipsService.rejectFriendRequest(friendRequest);
            if (friendRequest == null)
                MessageAlert.showErrorMessage(null, "Cerere de prietenie inexistenta!");
            else
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Reject", "Cerere de prietenie refuzata!");
        }
    }

    public void handleSetUser(ActionEvent actionEvent) {
        Utilizator utilizator = tableViewUsers.getSelectionModel().getSelectedItem();
        if (utilizator != null) {
            textFieldSetUser.setText(utilizator.getId());
        } else MessageAlert.showErrorMessage(null, "Nu ati selectat nici un utilizator!");
    }

    public void handleShowFriends(ActionEvent actionEvent) {
        if(textFieldSetUser.getText().isEmpty())
            MessageAlert.showErrorMessage(null, "Nu exista un utilizator curent!");
        else {
            Utilizator user = usersService.find(textFieldSetUser.getText());
            showFriendshipDialog(user);
        }
    }

    private void showFriendshipDialog(Utilizator user) {
        try {
            FXMLLoader loader = new FXMLLoader(UserApplication.class.getResource("friendship-view.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Prietenii");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            FriendshipsController controller = loader.getController();
            controller.setService(friendshipsService, dialogStage, user);
            dialogs.add(dialogStage);

            dialogStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void handleSendMessage(ActionEvent actionEvent) {
        if(textFieldSetUser.getText().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Nu exista un utilizator curent!");
            return;
        }
        Utilizator from = usersService.find(textFieldSetUser.getText());
        List<Utilizator> to = tableViewUsers.getSelectionModel().getSelectedItems();
        showSendDialog(from, to);
    }

    private void showSendDialog(Utilizator from, List<Utilizator> to) {
        try {
            FXMLLoader loader = new FXMLLoader(UserApplication.class.getResource("message-view.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Trimite mesaj");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            MessageController controller = loader.getController();
            controller.setService(usersService, dialogStage, from, to);
            dialogs.add(dialogStage);

            dialogStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void handleOpenChat(ActionEvent actionEvent) {
        if(textFieldSetUser.getText().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Nu exista un utilizator curent!");
            return;
        }
        Utilizator u1 = usersService.find(textFieldSetUser.getText());
        Utilizator u2 = tableViewUsers.getSelectionModel().getSelectedItem();
        if(u2 != null)
            showChatDialog(u1, u2);
        else
            MessageAlert.showErrorMessage(null, "Nu ati selectat nici un utilizator!");
    }

    private void showChatDialog(Utilizator u1, Utilizator u2) {
        try {
            FXMLLoader loader = new FXMLLoader(UserApplication.class.getResource("chat-view.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chat with " + u2.getFirstName() + " " + u2.getLastName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            ChatController controller = loader.getController();
            controller.setService(usersService, dialogStage, u1, u2);
            dialogs.add(dialogStage);

            dialogStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
