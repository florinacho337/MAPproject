package ro.ubbcluj.map.repository.pagingrepositories;

import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.repository.paging.Page;
import ro.ubbcluj.map.repository.paging.Pageable;
import ro.ubbcluj.map.repository.paging.PagingRepository;
import ro.ubbcluj.map.repository.paging.PageImplementation;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class UserDBPagingRepository extends UserDBRepository implements PagingRepository<String, Utilizator> {

    public UserDBPagingRepository(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public Page<Utilizator> findAll(Pageable pageable, String id) {
        Set<Utilizator> users = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users where username != ? limit ? offset ?")
        ) {
            statement.setString(1, id);
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, (pageable.getPageNumber()-1) * pageable.getPageSize());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
            {
                users.add(extractUser(resultSet));

            }
            return new PageImplementation<>(pageable, users.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
