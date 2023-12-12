package ro.ubbcluj.map.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.UserApplication;
import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.entities.dtos.FriendRequestDTO;
import ro.ubbcluj.map.domain.entities.dtos.PrietenDTO;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.exceptions.DuplicateException;
import ro.ubbcluj.map.utils.observer.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class UserController implements Observer<UtilizatorChangeEvent> {
    @FXML
    private TableColumn<Utilizator, String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator, String> tableColumnLastName;
    @FXML
    private TableColumn<PrietenDTO, String> tableColumnFirstNameFriends;
    @FXML
    private TableColumn<PrietenDTO, String> tableColumnLastNameFriends;
    @FXML
    private TableColumn<PrietenDTO, LocalDateTime> tableColumnFriendsFrom;
    @FXML
    private TableColumn<FriendRequestDTO, String> tableColumnFirstNameFR;
    @FXML
    private TableColumn<FriendRequestDTO, String> tableColumnLastNameFR;
    @FXML
    private TableColumn<FriendRequestDTO, String> tableColumnStatus;
    @FXML
    private TableView<FriendRequestDTO> tableViewFR;
    @FXML
    private TableView<PrietenDTO> tableViewFriends;
    @FXML
    private TableView<Utilizator> tableViewUsers;
    @FXML
    private ContextMenu contextMenuU;
    @FXML
    private ContextMenu contextMenuFR;

    @FXML
    private final MenuItem menuItemOpenChat = new MenuItem("Open Chat");
    @FXML
    private final MenuItem menuItemAccept = new MenuItem("Accept");
    @FXML
    private final MenuItem menuItemReject = new MenuItem("Reject");

    ObservableList<Utilizator> modelUsers = FXCollections.observableArrayList();
    ObservableList<FriendRequestDTO> modelFR = FXCollections.observableArrayList();
    ObservableList<PrietenDTO> modelFriends = FXCollections.observableArrayList();

    private UsersService usersService;
    private FriendshipsService friendshipsService;
    private Utilizator utilizator;
    private Stage stage;

    public void setUserService(UsersService usersService, FriendshipsService friendshipsService, Utilizator utilizator, Stage stage){
        this.usersService = usersService;
        this.friendshipsService = friendshipsService;
        this.utilizator = utilizator;
        this.stage = stage;
        usersService.addObserver(this);
        friendshipsService.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableColumnFirstNameFR.setCellValueFactory(new PropertyValueFactory<>("firstNameFrom"));
        tableColumnLastNameFR.setCellValueFactory(new PropertyValueFactory<>("lastNameFrom"));
        tableColumnFirstNameFriends.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastNameFriends.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableColumnFriendsFrom.setCellValueFactory(new PropertyValueFactory<>("friendsFrom"));
        tableViewUsers.setItems(modelUsers);
        tableViewUsers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewFR.setItems(modelFR);
        tableViewFriends.setItems(modelFriends);
        contextMenuU = new ContextMenu(menuItemOpenChat);
        tableViewFriends.setContextMenu(contextMenuU);
        tableViewUsers.setContextMenu(contextMenuU);
        contextMenuFR = new ContextMenu(menuItemAccept, menuItemReject);
        tableViewFR.setContextMenu(contextMenuFR);

    }

    public void handleSendFR(ActionEvent actionEvent) {
        try {
            Utilizator to = tableViewUsers.getSelectionModel().getSelectedItem();
            if (friendshipsService.sendFriendRequest(utilizator, to) == null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Friend Request", "Cerere de prietenie trimisa cu succes!");
            else
                MessageAlert.showErrorMessage(null, "Cerere de prietenie existenta!");
        } catch (DuplicateException e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void handleSendMessage(ActionEvent actionEvent) {
        List<Utilizator> to = tableViewUsers.getSelectionModel().getSelectedItems();
        showSendDialog(to);
    }

    private void showSendDialog(List<Utilizator> to) {
        try {
            FXMLLoader loader = new FXMLLoader(UserApplication.class.getResource("message-view.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Trimite mesaj");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            MessageController controller = loader.getController();
            controller.setService(usersService, dialogStage, utilizator, to);

            dialogStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void handleExit(ActionEvent actionEvent) {
        this.stage.close();
    }

    @Override
    public void update(UtilizatorChangeEvent utilizatorChangeEvent) {
        initModel();
    }

    private void initModel() {
        initFriends();
        initFR();
        initUsers();
    }

    private void initUsers() {
        List<Utilizator> usersList = StreamSupport.stream(usersService.getAll().spliterator(), false).filter(user -> !(Objects.equals(user.getId(), utilizator.getId()))).toList();
        modelUsers.setAll(usersList);
    }

    private void initFR() {
        Iterable<FriendRequest> friendRequests = friendshipsService.getFriendRequests();
        List<FriendRequestDTO> friendRequestDTOList = StreamSupport.stream(friendRequests.spliterator(), false)
                .map(FriendRequestDTO::new)
                .filter(friendRequestDTO -> Objects.equals(friendRequestDTO.getTo(), utilizator.getId()))
                .toList();
        modelFR.setAll(friendRequestDTOList);
    }

    private void initFriends() {
        Iterable<Prietenie> friendships = friendshipsService.getAll();
        List<PrietenDTO> prieteniiUser = StreamSupport.stream(friendships.spliterator(), false)
                .filter(prietenie -> Objects.equals(prietenie.getU1().getId(), utilizator.getId()) || Objects.equals(prietenie.getU2().getId(), utilizator.getId()))
                .map(prietenie -> new PrietenDTO(prietenie, utilizator)).toList();
        modelFriends.setAll(prieteniiUser);
    }

    public void contextMenuTableUsers(ContextMenuEvent contextMenuEvent) {
        menuItemOpenChat.setOnAction(event -> {
            Utilizator u2 = tableViewUsers.getSelectionModel().getSelectedItem();
            try {
                showChatDialog(u2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void showChatDialog(Utilizator user) throws IOException {
        FXMLLoader loader = new FXMLLoader(UserApplication.class.getResource("chat-view.fxml"));

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Chat with " + user.getFirstName() + " " + user.getLastName());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setScene(new Scene(loader.load()));

        ChatController controller = loader.getController();
        controller.setService(usersService, dialogStage, utilizator, user);

        dialogStage.show();
    }

    public void contextMenuTableFR(ContextMenuEvent contextMenuEvent) {
        menuItemAccept.setOnAction(event -> {
            FriendRequestDTO friendRequestDTO = tableViewFR.getSelectionModel().getSelectedItem();
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
        });

        menuItemReject.setOnAction(event -> {
            FriendRequestDTO friendRequestDTO = tableViewFR.getSelectionModel().getSelectedItem();
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
        });
    }
}
