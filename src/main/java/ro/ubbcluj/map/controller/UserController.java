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
    private Pagination usersPagination;
    @FXML
    private Pagination friendsPagination;
    @FXML
    private Pagination friendRequestsPagination;
    @FXML
    private TextField textFieldMaxEntities;
    @FXML
    private TextField textFieldMaxUsers;
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
    private int usersPageSize;
    private int entitiesPageSize;
    private Stage stage;

    public void setService(UsersService usersService, FriendshipsService friendshipsService, Utilizator utilizator, Stage stage) {
        this.usersService = usersService;
        this.friendshipsService = friendshipsService;
        this.utilizator = utilizator;
        this.stage = stage;
        usersService.addObserver(this);
        friendshipsService.addObserver(this);
        usersPageSize = 10;
        entitiesPageSize = 10;
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
        ContextMenu contextMenuU = new ContextMenu(menuItemOpenChat);
        tableViewFriends.setContextMenu(contextMenuU);
        tableViewUsers.setContextMenu(contextMenuU);
        ContextMenu contextMenuFR = new ContextMenu(menuItemAccept, menuItemReject);
        tableViewFR.setContextMenu(contextMenuFR);
        usersPagination.setMaxPageIndicatorCount(3);
        friendsPagination.setMaxPageIndicatorCount(3);
        friendRequestsPagination.setMaxPageIndicatorCount(3);
        usersPagination.setPageFactory(param -> {
            initUsers(param+1);
            return tableViewUsers;
        });
        friendsPagination.setPageFactory(param -> {
            initFriends(param+1);
            return tableViewFriends;
        });
        friendRequestsPagination.setPageFactory(param -> {
            initFR(param+1);
            return tableViewFR;
        });
    }

    public void handleSendFR(ActionEvent actionEvent) {
        try {
            Utilizator to = tableViewUsers.getSelectionModel().getSelectedItem();
            if (friendshipsService.sendFriendRequest(utilizator, to) == null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Friend Request", "Cerere de prietenie trimisa cu succes!");
            else
                MessageAlert.showErrorMessage(null, "Cerere de prietenie existenta!");
        } catch (DuplicateException e) {
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
        } catch (IOException e) {
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
        initFriends(1);
        initFR(1);
        initUsers(1);
    }

    private void initUsers(int page) {
        List<Utilizator> users = StreamSupport.stream(usersService.getAll().spliterator(), false)
                .filter(utilizator1 -> !Objects.equals(utilizator1.getId(), utilizator.getId()))
                .toList();
        if(users.size() < usersPageSize)
            usersPagination.setPageCount(1);
        else
            usersPagination.setPageCount(users.size() / usersPageSize + users.size() % usersPageSize);
        usersService.setPageSize(usersPageSize);
        List<Utilizator> usersList = usersService.getUsersOnPage(page, utilizator).stream().toList();
        modelUsers.setAll(usersList);
    }

    private void initFR(int page) {
        List<FriendRequest> friendRequests = StreamSupport.stream(friendshipsService.getFriendRequests().spliterator(), false)
                .filter(friendRequest -> Objects.equals(friendRequest.getTo().getId(), utilizator.getId()))
                .toList();
        if(friendRequests.size() < entitiesPageSize)
            friendRequestsPagination.setPageCount(1);
        else
            friendRequestsPagination.setPageCount(friendRequests.size() / entitiesPageSize + friendRequests.size() % entitiesPageSize);
        friendshipsService.setPageSize(entitiesPageSize);
        List<FriendRequestDTO> friendRequestDTOList = friendshipsService.getFriendRequestsOnPage(page, utilizator)
                .stream()
                .map(FriendRequestDTO::new)
                .toList();
        modelFR.setAll(friendRequestDTOList);
    }

    private void initFriends(int page) {
        List<Prietenie> prietenii = StreamSupport.stream(friendshipsService.getAll().spliterator(), false)
                .filter(prietenie -> Objects.equals(prietenie.getU1().getUsername(), utilizator.getId()) || Objects.equals(prietenie.getU2().getUsername(), utilizator.getId()))
                .toList();
        if(prietenii.size() < entitiesPageSize)
            friendsPagination.setPageCount(1);
        else
            friendsPagination.setPageCount(prietenii.size() / entitiesPageSize + prietenii.size() % entitiesPageSize);
        List<PrietenDTO> prieteniiUser = friendshipsService.getFriendshipsOnPage(page, utilizator)
                .stream()
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
            if (Objects.equals(friendRequest.getStatus(), "approved") || Objects.equals(friendRequest.getStatus(), "rejected"))
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

    public void handleSetMaxUsers(ActionEvent actionEvent) {
        if (textFieldMaxUsers.getText().isBlank()) {
            MessageAlert.showErrorMessage(null, "Nu ati setat nici o valoare!");
            return;
        }
        this.usersPageSize = Integer.parseInt(textFieldMaxUsers.getText());
        initUsers(1);
    }

    public void handleSetMaxEntities(ActionEvent actionEvent) {
        if (textFieldMaxEntities.getText().isBlank()) {
            MessageAlert.showErrorMessage(null, "Nu ati setat nici o valoare!");
            return;
        }
        this.entitiesPageSize = Integer.parseInt(textFieldMaxEntities.getText());
        initFR(1);
        initFriends(1);
    }
}
