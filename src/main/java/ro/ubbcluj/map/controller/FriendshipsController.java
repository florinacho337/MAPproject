package ro.ubbcluj.map.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.entities.dtos.PrietenDTO;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class FriendshipsController implements Observer<UtilizatorChangeEvent>, Controller {
    @FXML
    private TableColumn<PrietenDTO, String> tableColumnFirstName;
    @FXML
    private TableColumn<PrietenDTO, String> tableColumnLastName;
    @FXML
    private TableColumn<PrietenDTO, LocalDateTime> tableColumnFriendsFrom;
    @FXML
    private TableColumn<PrietenDTO, Long> tableColumnId;
    @FXML
    private TableView<PrietenDTO> tableView;
    private Stage dialogStage;

    private FriendshipsService friendshipsService;
    private Utilizator u;
    private final ObservableList<PrietenDTO> model = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableColumnFriendsFrom.setCellValueFactory(new PropertyValueFactory<>("friendsFrom"));
        tableView.setItems(model);
    }
    public void setService(FriendshipsService friendshipsService, Stage dialogStage, Utilizator u){
        this.friendshipsService = friendshipsService;
        this.dialogStage = dialogStage;
        this.u = u;
        friendshipsService.addObserver(this);
        initModel();
    }

    private void initModel(){
        Iterable<Prietenie> friendships = friendshipsService.getAll();
        List<PrietenDTO> prieteniiUser = StreamSupport.stream(friendships.spliterator(), false)
                .filter(prietenie -> Objects.equals(prietenie.getU1().getId(), u.getId()) || Objects.equals(prietenie.getU2().getId(), u.getId()))
                .map(prietenie -> new PrietenDTO(prietenie, u)).toList();
        model.setAll(prieteniiUser);
    }
    public void handleExit(ActionEvent actionEvent) {
        this.dialogStage.close();
    }

    @Override
    public void update(UtilizatorChangeEvent utilizatorChangeEvent) {
        initModel();
    }

    @Override
    public void close() {
        this.dialogStage.close();
    }
}
