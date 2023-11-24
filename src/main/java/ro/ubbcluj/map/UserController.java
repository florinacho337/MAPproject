package ro.ubbcluj.map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.controller.EditUserController;
import ro.ubbcluj.map.controller.MessageAlert;
import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.observer.Observer;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

public class UserController implements Observer<UtilizatorChangeEvent> {
    UsersService usersService;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();
    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator, String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator, String> tableColumnLastName;
    private Stage stage;

    public void setUsersService(UsersService usersService, Stage stage){
        this.usersService = usersService;
        this.stage = stage;
        usersService.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableView.setItems(model);
    }

    private void initModel() {
        Iterable<Utilizator> users = usersService.getAll();
        List<Utilizator> usersList = StreamSupport.stream(users.spliterator(), false).toList();
        model.setAll(usersList);
    }

    @Override
    public void update(UtilizatorChangeEvent e) {
        initModel();
    }

    public void onAddButtonClick(ActionEvent actionEvent) {
        showEditUserDialog(null);
    }

    public void onUpdateButtonClick(ActionEvent actionEvent) {
        Utilizator user = tableView.getSelectionModel().getSelectedItem();
        if(user != null)
            showEditUserDialog(user);
        else
            MessageAlert.showErrorMessage(null, "Nu ati selectat nici un utilizator!");
    }

    private void showEditUserDialog(Utilizator user) {
        try {
            FXMLLoader loader = new FXMLLoader(UserController.class.getResource("edituser-view.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(loader.load()));

            EditUserController controller = loader.getController();
            controller.setService(usersService, dialogStage, user);

            dialogStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void onDeleteButtonClick(ActionEvent actionEvent){
        Utilizator selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Utilizator deleted = usersService.remove(selected.getId());
            if (null != deleted)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Delete", "Utilizatorul a fost sters cu succes!");
        } else MessageAlert.showErrorMessage(null, "Nu ati selectat nici un utilizator!");
    }

    public void handleExit(ActionEvent actionEvent) {
        stage.close();
    }
}
