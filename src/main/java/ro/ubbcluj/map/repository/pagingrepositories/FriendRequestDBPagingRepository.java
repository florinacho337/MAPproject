package ro.ubbcluj.map.repository.pagingrepositories;

import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.repository.dbrepositories.FriendRequestDBRepo;
import ro.ubbcluj.map.repository.paging.Page;
import ro.ubbcluj.map.repository.paging.PageImplementation;
import ro.ubbcluj.map.repository.paging.Pageable;
import ro.ubbcluj.map.repository.paging.PagingRepository;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class FriendRequestDBPagingRepository extends FriendRequestDBRepo implements PagingRepository<Long, FriendRequest> {
    public FriendRequestDBPagingRepository(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public Page<FriendRequest> findAll(Pageable pageable, String id) {
        Set<FriendRequest> friendRequests = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("""
                     select fr.id, "from", "to", status, u1.first_name as "firstNameFrom", u1.last_name as "lastNameFrom", u1.password as "passwordFrom", u2.first_name as "firstNameTo", u2.last_name as "lastNameTo", u2.password as "passwordTo" from friend_requests fr
                                          inner join users u1 on u1.username = fr.from
                                          inner join users u2 on u2.username = fr.to
                                          where "to" = ?
                                          limit ? offset ?""")
        ) {
            statement.setString(1, id);
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, (pageable.getPageNumber()-1) * pageable.getPageSize());
            ResultSet resultSet = statement.executeQuery();
            extractResult(resultSet, friendRequests);
            return new PageImplementation<>(pageable, friendRequests.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
