package ro.ubbcluj.map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ro.ubbcluj.map.controller.LoginRegisterController;
import ro.ubbcluj.map.repository.dbrepositories.MessageDBRepository;
import ro.ubbcluj.map.repository.pagingrepositories.FriendRequestDBPagingRepository;
import ro.ubbcluj.map.repository.pagingrepositories.FriendshipDBPagingRepository;
import ro.ubbcluj.map.repository.pagingrepositories.UserDBPagingRepository;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.DBConnection;

import java.io.IOException;
import java.util.ArrayList;

public class UserApplication extends Application {
    private UsersService usersService;
    private FriendshipsService friendshipsService;
    private final DBConnection dbConnection = new DBConnection();
    @Override
    public void start(Stage primaryStage) throws Exception {
        UserDBPagingRepository repoUsers = new UserDBPagingRepository(dbConnection.getConnection());
        FriendRequestDBPagingRepository friendRequestDBRepo = new FriendRequestDBPagingRepository(dbConnection.getConnection());
        FriendshipDBPagingRepository friendshipDBRepository = new FriendshipDBPagingRepository(dbConnection.getConnection());
        friendshipsService = new FriendshipsService(friendshipDBRepository, repoUsers, friendRequestDBRepo);
        MessageDBRepository repoMessages = new MessageDBRepository(dbConnection.getConnection());
        usersService = new UsersService(repoUsers, repoMessages);

        initView(primaryStage);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader usersLoader = new FXMLLoader(UserApplication.class.getResource("login-view.fxml"));
        primaryStage.setScene(new Scene(usersLoader.load()));
        primaryStage.setTitle("Login");

        LoginRegisterController loginRegisterController = usersLoader.getController();
        loginRegisterController.setUsersService(usersService, friendshipsService, primaryStage, dbConnection, new ArrayList<>());

    }

    public static void main(String[] args) {
        launch(args);
    }
}
