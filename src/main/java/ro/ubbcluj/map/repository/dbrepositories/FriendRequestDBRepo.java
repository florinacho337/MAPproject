package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.FriendRequestValidator;
import ro.ubbcluj.map.repository.Repository;

import javax.security.auth.login.AccountLockedException;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FriendRequestDBRepo implements Repository<Long, FriendRequest> {
    private final String url;
    private final String username;
    private final String password;
    private final FriendRequestValidator validator;

    public FriendRequestDBRepo(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = new FriendRequestValidator();
    }

    private void setFriends(Utilizator utilizator){
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from friendships "+
                    "where id_u1 = ? or id_u2 = ?")
        ){
            statement.setInt(1, Math.toIntExact(utilizator.getId()));
            statement.setInt(2, Math.toIntExact(utilizator.getId()));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                PreparedStatement getFriend = connection.prepareStatement("select * from users "+
                        "where id = ?");
                if(resultSet.getInt("id_u1") == utilizator.getId())
                    getFriend.setInt(1, resultSet.getInt("id_u2"));
                else
                    getFriend.setInt(1, resultSet.getInt("id_u1"));
                ResultSet friend = getFriend.executeQuery();
                if(friend.next()){
                    String firstName = friend.getString("first_name");
                    String lastName = friend.getString("last_name");
                    Long id = (long) friend.getInt("id");
                    Utilizator prieten = new Utilizator(firstName, lastName);
                    prieten.setId(id);
                    utilizator.addFriend(prieten);
                }
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    public Optional<FriendRequest> findOne(Long aLong) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("""
                     select "from", "to", status, u1.first_name as "firstNameFrom", u1.last_name as "lastNameFrom", u2.first_name as "firstNameTo", u2.last_name as "lastNameTo" from friend_requests fr
                     inner join users u1 on u1.id = fr.from
                     inner join users u2 on u2.id = fr.to
                     where fr.id = ?
                     """)

        ) {
            statement.setInt(1, Math.toIntExact(aLong));
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String firstNameFrom = resultSet.getString("firstNameFrom");
                String lastNameFrom = resultSet.getString("lastNameFrom");
                String fristNameTo = resultSet.getString("firstNameTo");
                String lastNameTo = resultSet.getString("lastNameTo");
                Long id_from = resultSet.getLong("from");
                Long id_to = resultSet.getLong("to");
                Utilizator from = new Utilizator(firstNameFrom,lastNameFrom);
                Utilizator to = new Utilizator(fristNameTo, lastNameTo);
                from.setId(id_from);
                to.setId(id_to);
                setFriends(from);
                setFriends(to);
                FriendRequest friendRequest = new FriendRequest(from, to);
                return Optional.of(friendRequest);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        Set<FriendRequest> friendRequests = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("""
                     select fr.id, "from", "to", status, u1.first_name as "firstNameFrom", u1.last_name as "lastNameFrom", u2.first_name as "firstNameTo", u2.last_name as "lastNameTo" from friend_requests fr
                     inner join users u1 on u1.id = fr.from
                     inner join users u2 on u2.id = fr.to
                     """);
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next())
            {
                Long from = resultSet.getLong("from");
                Long to = resultSet.getLong("to");
                Long id = resultSet.getLong("id");
                String status = resultSet.getString("status");
                String fristNameU1 = resultSet.getString("firstNameFrom");
                String fristNameU2 = resultSet.getString("firstNameTo");
                String lastNameU1 = resultSet.getString("lastNameFrom");
                String lastNameU2 = resultSet.getString("lastNameTo");
                Utilizator u1 = new Utilizator(fristNameU1, lastNameU1);
                Utilizator u2 = new Utilizator(fristNameU2, lastNameU2);
                u1.setId(from);
                u2.setId(to);
                setFriends(u1);
                setFriends(u2);
                FriendRequest friendRequest = new FriendRequest(u1, u2, status);
                friendRequest.setId(id);
                friendRequests.add(friendRequest);
            }
            return friendRequests;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into friend_requests(\"from\", \"to\") " +
                    "values (?, ?)")
        ){
            statement.setInt(1, Math.toIntExact(entity.getFrom().getId()));
            statement.setInt(2, Math.toIntExact(entity.getTo().getId()));
            int response = statement.executeUpdate();
            if(response != 0)
                return Optional.empty();
            else
                return Optional.of(entity);
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> delete(Long aLong) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from friend_requests where id = ?")
        ){
            statement.setInt(1, Math.toIntExact(aLong));
            Optional<FriendRequest> friendRequest = findOne(aLong);
            int response = statement.executeUpdate();
            if(response != 0)
                return friendRequest;
            else
                return Optional.empty();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("update friend_requests set status = ? where id = ?")
        ){
            statement.setString(1, entity.getStatus());
            statement.setInt(2, Math.toIntExact(entity.getId()));
            int response = statement.executeUpdate();
            if(response != 0)
                return Optional.empty();
            else
                return Optional.of(entity);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
