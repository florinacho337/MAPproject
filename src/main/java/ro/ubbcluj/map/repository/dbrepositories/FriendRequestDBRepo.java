package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.FriendRequestValidator;
import ro.ubbcluj.map.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class FriendRequestDBRepo implements Repository<Long, FriendRequest> {
    protected final Connection connection;
    private final FriendRequestValidator validator;

    public FriendRequestDBRepo(Connection connection) {
        this.connection = connection;
        this.validator = new FriendRequestValidator();
    }

    protected void setFriends(Utilizator utilizator){
        try(PreparedStatement statement = connection.prepareStatement("select * from friendships "+
                    "where username1 = ? or username2 = ?")
        ){
            statement.setString(1, utilizator.getId());
            statement.setString(2, utilizator.getId());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                PreparedStatement getFriend = connection.prepareStatement("select * from users "+
                        "where username = ?");
                if(Objects.equals(resultSet.getString("username1"), utilizator.getId()))
                    getFriend.setString(1, resultSet.getString("username2"));
                else
                    getFriend.setString(1, resultSet.getString("username1"));
                ResultSet friend = getFriend.executeQuery();
                if(friend.next()){
                    String firstName = friend.getString("first_name");
                    String lastName = friend.getString("last_name");
                    String password = friend.getString("password");
                    String id = friend.getString("username");
                    Utilizator prieten = new Utilizator(firstName, lastName, id, password);
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
        try(PreparedStatement statement = connection.prepareStatement("""
                     select "from", "to", status, u1.first_name as "firstNameFrom", u1.last_name as "lastNameFrom", u1.password as "passwordU1", u2.first_name as "firstNameTo", u2.last_name as "lastNameTo", u2.password as "passwordU2" from friend_requests fr
                     inner join users u1 on u1.username = fr.from
                     inner join users u2 on u2.username = fr.to
                     where fr.id = ?
                     """)

        ) {
            statement.setInt(1, Math.toIntExact(aLong));
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String firstNameFrom = resultSet.getString("firstNameFrom");
                String lastNameFrom = resultSet.getString("lastNameFrom");
                String passwordFrom = resultSet.getString("passwordU1");
                String fristNameTo = resultSet.getString("firstNameTo");
                String lastNameTo = resultSet.getString("lastNameTo");
                String passwordTo = resultSet.getString("passwordU2");
                String id_from = resultSet.getString("from");
                String id_to = resultSet.getString("to");
                Utilizator from = new Utilizator(firstNameFrom,lastNameFrom, id_from, passwordFrom);
                Utilizator to = new Utilizator(fristNameTo, lastNameTo, id_to, passwordTo);
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

        try (PreparedStatement statement = connection.prepareStatement("""
                     select fr.id, "from", "to", status, u1.first_name as "firstNameFrom", u1.last_name as "lastNameFrom", u1.password as "passwordFrom", u2.first_name as "firstNameTo", u2.last_name as "lastNameTo", u2.password as "passwordTo" from friend_requests fr
                     inner join users u1 on u1.username = fr.from
                     inner join users u2 on u2.username = fr.to
                     """);
             ResultSet resultSet = statement.executeQuery()
        ) {

            extractResult(resultSet, friendRequests);
            return friendRequests;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void extractResult(ResultSet resultSet, Set<FriendRequest> friendRequests) throws SQLException {
        while (resultSet.next())
        {
            String from = resultSet.getString("from");
            String to = resultSet.getString("to");
            Long id = resultSet.getLong("id");
            String status = resultSet.getString("status");
            String fristNameU1 = resultSet.getString("firstNameFrom");
            String fristNameU2 = resultSet.getString("firstNameTo");
            String passwordFrom = resultSet.getString("passwordFrom");
            String lastNameU1 = resultSet.getString("lastNameFrom");
            String lastNameU2 = resultSet.getString("lastNameTo");
            String passwordTo = resultSet.getString("passwordTo");
            Utilizator u1 = new Utilizator(fristNameU1, lastNameU1, from, passwordFrom);
            Utilizator u2 = new Utilizator(fristNameU2, lastNameU2, to, passwordTo);
            u1.setId(from);
            u2.setId(to);
            setFriends(u1);
            setFriends(u2);
            FriendRequest friendRequest = new FriendRequest(u1, u2, status);
            friendRequest.setId(id);
            friendRequests.add(friendRequest);
        }
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        validator.validate(entity);
        try(PreparedStatement statement = connection.prepareStatement("insert into friend_requests(\"from\", \"to\") " +
                    "values (?, ?)")
        ){
            statement.setString(1, entity.getFrom().getId());
            statement.setString(2, entity.getTo().getId());
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
        try(PreparedStatement statement = connection.prepareStatement("delete from friend_requests where id = ?")
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
        try(PreparedStatement statement = connection.prepareStatement("update friend_requests set status = ? where id = ?")
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
