package ro.ubbcluj.map.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.map.UserApplication;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.DBConnection;
import ro.ubbcluj.map.utils.PasswordEncoder;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LoginRegisterController {

    @FXML
    private PasswordField passwordFieldLogin;
    @FXML
    private TextField usernameFieldLogin;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameFieldRegister;
    @FXML
    private PasswordField passwordFieldRegister;
    private UsersService usersService;
    private FriendshipsService friendshipsService;
    private Stage stage;
    private DBConnection dbConnection;
    private List<Controller> controllers;

    public void setUsersService(UsersService usersService, FriendshipsService friendshipsService, Stage stage, DBConnection dbConnection, List<Controller> controllers) {
        this.friendshipsService = friendshipsService;
        this.usersService = usersService;
        this.stage = stage;
        this.dbConnection = dbConnection;
        this.controllers = controllers;
        initStage();
    }

    private void initStage() {
        stage.setOnCloseRequest(event -> {
            controllers.forEach(Controller::close);
            try {
                dbConnection.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void changeToRegister(MouseEvent mouseEvent) throws IOException {
        FXMLLoader registerLoader = new FXMLLoader(UserApplication.class.getResource("register-view.fxml"));
        stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(registerLoader.load()));
        stage.setTitle("Register");

        LoginRegisterController loginRegisterController = registerLoader.getController();
        loginRegisterController.setUsersService(usersService, friendshipsService, stage, dbConnection, controllers);

        stage.show();
    }

    public void changeToLogin(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loginLoader = new FXMLLoader(UserApplication.class.getResource("login-view.fxml"));
        stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loginLoader.load()));
        stage.setTitle("Login");

        LoginRegisterController loginRegisterController = loginLoader.getController();
        loginRegisterController.setUsersService(usersService, friendshipsService, stage, dbConnection, controllers);

        stage.show();
    }

    public void handleLogin(ActionEvent actionEvent) throws IOException, NoSuchAlgorithmException {
        String password = passwordFieldLogin.getText();
        String username = usernameFieldLogin.getText();
        Utilizator utilizator = usersService.find(username);
        if (utilizator == null || !Objects.equals(utilizator.getPassword(), PasswordEncoder.encrypt(password))) {
            MessageAlert.showErrorMessage(null, "Username sau parola gresita!");
            this.passwordFieldLogin.clear();
            return;
        }
        if (Objects.equals(utilizator.getId(), "florWIN"))
            showAdminDialog();
        else
            showUserDialog(utilizator);
        usernameFieldLogin.clear();
        passwordFieldLogin.clear();
    }

    private void showAdminDialog() throws IOException {
        FXMLLoader usersLoader = new FXMLLoader(UserApplication.class.getResource("admin-view.fxml"));
        Stage adminStage = new Stage();
        adminStage.setScene(new Scene(usersLoader.load()));
        adminStage.initModality(Modality.WINDOW_MODAL);
        adminStage.setTitle("ADMIN");

        AdminController adminController = usersLoader.getController();
        adminController.setUsersService(usersService, friendshipsService, adminStage);
        controllers.add(adminController);

        adminStage.show();
    }

    private void showUserDialog(Utilizator utilizator) throws IOException {
        FXMLLoader usersLoader = new FXMLLoader(UserApplication.class.getResource("user-view.fxml"));
        Stage userStage = new Stage();
        userStage.setScene(new Scene(usersLoader.load()));
        userStage.initModality(Modality.WINDOW_MODAL);
        userStage.setTitle(utilizator.getFirstName() + " " + utilizator.getLastName());

        UserController userController = usersLoader.getController();
        userController.setService(usersService, friendshipsService, utilizator, userStage);
        controllers.add(userController);

        userStage.show();
    }

    public void handleRegister(ActionEvent actionEvent) throws NoSuchAlgorithmException {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameFieldRegister.getText();
        String password = passwordFieldRegister.getText();
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            MessageAlert.showErrorMessage(null, "Exista spatii necompletate!");
            return;
        }
        if (usersService.find(username) != null) {
            MessageAlert.showErrorMessage(null, "Exista deja un utilizator cu acest username!");
            return;
        }
        usersService.add(new Utilizator(firstName, lastName, username, PasswordEncoder.encrypt(password)));
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Register", "Utilizator inregistrat cu succes!");
        firstNameField.clear();
        lastNameField.clear();
        usernameFieldRegister.clear();
        passwordFieldRegister.clear();
    }

    public void setOnKeyPressedPasswordLogin(KeyEvent keyEvent) throws IOException, NoSuchAlgorithmException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleLogin(new ActionEvent());
        } else if(keyEvent.getCode().equals(KeyCode.UP)){
            usernameFieldLogin.requestFocus();
        }
    }

    public void setOnKeyPressedUsernameLogin(KeyEvent keyEvent) throws NoSuchAlgorithmException, IOException {
        if(keyEvent.getCode().equals(KeyCode.DOWN)){
            passwordFieldLogin.requestFocus();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleLogin(new ActionEvent());
        }
    }

    public void setOnKeyPressedFN(KeyEvent keyEvent) throws NoSuchAlgorithmException {
        if(keyEvent.getCode().equals(KeyCode.DOWN)){
            lastNameField.requestFocus();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleRegister(new ActionEvent());
        }
    }

    public void setOnKeyPressedLN(KeyEvent keyEvent) throws NoSuchAlgorithmException {
        if(keyEvent.getCode().equals(KeyCode.DOWN)){
            usernameFieldRegister.requestFocus();
        } else if(keyEvent.getCode().equals(KeyCode.UP)){
            firstNameField.requestFocus();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleRegister(new ActionEvent());
        }
    }

    public void setOnKeyPressedUsernameRegister(KeyEvent keyEvent) throws NoSuchAlgorithmException {
        if(keyEvent.getCode().equals(KeyCode.DOWN)){
            passwordFieldRegister.requestFocus();
        } else if(keyEvent.getCode().equals(KeyCode.UP)){
            lastNameField.requestFocus();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleRegister(new ActionEvent());
        }
    }

    public void setOnKeyPressedPasswordRegister(KeyEvent keyEvent) throws NoSuchAlgorithmException {
        if(keyEvent.getCode().equals(KeyCode.UP)){
            usernameFieldRegister.requestFocus();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            handleRegister(new ActionEvent());
        }
    }
}
