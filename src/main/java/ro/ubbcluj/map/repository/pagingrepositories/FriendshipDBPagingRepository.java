package ro.ubbcluj.map.repository.pagingrepositories;

import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Tuple;
import ro.ubbcluj.map.repository.dbrepositories.FriendshipDBRepository;
import ro.ubbcluj.map.repository.paging.Page;
import ro.ubbcluj.map.repository.paging.PageImplementation;
import ro.ubbcluj.map.repository.paging.Pageable;
import ro.ubbcluj.map.repository.paging.PagingRepository;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class FriendshipDBPagingRepository extends FriendshipDBRepository implements PagingRepository<Tuple<String, String>, Prietenie> {
    public FriendshipDBPagingRepository(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public Page<Prietenie> findAll(Pageable pageable, String id) {
        Set<Prietenie> prietenii = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("""
                     select username1, username2, "friendsFrom", u1.first_name as "firstNameU1", u1.last_name as "lastNameU1", u1.password as "passwordU1", u2.first_name as "firstNameU2", u2.last_name as "lastNameU2", u2.password as "passwordU2" from friendships f
                                          inner join users u1 on u1.username = f.username1
                                          inner join users u2 on u2.username = f.username2
                                          where username1 = ? or username2 = ?
                                          limit ? offset ?""")
        ) {

            statement.setString(1, id);
            statement.setString(2, id);
            statement.setInt(3, pageable.getPageSize());
            statement.setInt(4, (pageable.getPageNumber()-1) * pageable.getPageSize());
            ResultSet resultSet = statement.executeQuery();
            extractResult(prietenii, resultSet);
            return new PageImplementation<>(pageable, prietenii.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
