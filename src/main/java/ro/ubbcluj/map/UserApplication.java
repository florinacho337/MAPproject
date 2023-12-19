package ro.ubbcluj.map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ro.ubbcluj.map.controller.LoginRegisterController;
import ro.ubbcluj.map.repository.dbrepositories.FriendRequestDBRepo;
import ro.ubbcluj.map.repository.dbrepositories.FriendshipDBRepository;
import ro.ubbcluj.map.repository.dbrepositories.MessageDBRepository;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.repository.pagingrepositories.FriendRequestDBPagingRepository;
import ro.ubbcluj.map.repository.pagingrepositories.FriendshipDBPagingRepository;
import ro.ubbcluj.map.repository.pagingrepositories.UserDBPagingRepository;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;

import java.io.IOException;

public class UserApplication extends Application {
    private UsersService usersService;
    private FriendshipsService friendshipsService;
    @Override
    public void start(Stage primaryStage) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/socialnetwork";
        String username = "postgres";
        String password = "postgres";
        UserDBPagingRepository repoUsers = new UserDBPagingRepository(url, username, password);
        FriendRequestDBPagingRepository friendRequestDBRepo = new FriendRequestDBPagingRepository(url, username, password);
        FriendshipDBPagingRepository friendshipDBRepository = new FriendshipDBPagingRepository(url, username, password);
        friendshipsService = new FriendshipsService(friendshipDBRepository, repoUsers, friendRequestDBRepo);
        MessageDBRepository repoMessages = new MessageDBRepository(url,username, password);
        usersService = new UsersService(repoUsers, repoMessages);

        initView(primaryStage);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader usersLoader = new FXMLLoader(UserApplication.class.getResource("login-view.fxml"));
        primaryStage.setScene(new Scene(usersLoader.load()));
        primaryStage.setTitle("Login");

        LoginRegisterController loginRegisterController = usersLoader.getController();
        loginRegisterController.setUsersService(usersService, friendshipsService, primaryStage);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
