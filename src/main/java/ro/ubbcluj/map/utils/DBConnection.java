package ro.ubbcluj.map.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection implements AutoCloseable{
    private static final Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/socialnetwork", "postgres", "postgres");
            System.out.println("CONNECTION IS OPENED!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DBConnection() {
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
        System.out.println("CONNECTION IS CLOSED!");
    }
}
