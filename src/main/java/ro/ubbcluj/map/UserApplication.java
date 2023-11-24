package ro.ubbcluj.map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.service.UsersService;

import java.io.IOException;

public class UserApplication extends Application {
    private UsersService usersService;
    @Override
    public void start(Stage primaryStage) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/socialnetwork";
        String username = "postgres";
        String password = "postgres";
        UserDBRepository repoUsers = new UserDBRepository(url, username, password);
        usersService = new UsersService(repoUsers);

        initView(primaryStage);
        primaryStage.show();
    }
    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader usersLoader = new FXMLLoader(UserApplication.class.getResource("user-view.fxml"));
        primaryStage.setScene(new Scene(usersLoader.load()));

        UserController userController = usersLoader.getController();
        userController.setUsersService(usersService, primaryStage);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
