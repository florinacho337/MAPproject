package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.UtilizatorValidator;
import ro.ubbcluj.map.domain.validators.Validator;
import ro.ubbcluj.map.repository.Repository;

import java.sql.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UserDBRepository implements Repository<String, Utilizator> {

    protected final Connection connection;
    private final Validator<Utilizator> validator;

    public UserDBRepository(Connection connection) {
        this.connection = connection;
        validator = new UtilizatorValidator();
    }

    private void setFriends(Utilizator utilizator){
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
                    String id = friend.getString("username");
                    String password = friend.getString("password");
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
    public Optional<Utilizator> findOne(String longID) {
        try(PreparedStatement statement = connection.prepareStatement("select * from users " +
                    "where username = ?")

        ) {
            statement.setString(1, longID);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                Utilizator u = new Utilizator(firstName,lastName, longID, password);
                u.setId(longID);
                setFriends(u);
                return Optional.of(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public  Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();

        try (PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next())
            {
                Utilizator user = extractUser(resultSet);
                setFriends(user);
                users.add(user);
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        validator.validate(entity);
        try(PreparedStatement statement = connection.prepareStatement("insert into users(first_name, last_name, username, password) " +
                    "values (?, ?, ?, ?)")
        ){
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getUsername());
            statement.setString(4, entity.getPassword());
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
    public Optional<Utilizator> delete(String ID) {
        try(PreparedStatement statement = connection.prepareStatement("delete from users where username = ?")
        ){
            statement.setString(1, ID);
            Optional<Utilizator> user = findOne(ID);
            int response = statement.executeUpdate();
            if(response != 0)
                return user;
            else
                return Optional.empty();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        try(PreparedStatement statement = connection.prepareStatement("update users set first_name = ?, last_name = ?, password = ? where username = ?")
        ){
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getId());
            int response = statement.executeUpdate();
            if(response != 0)
                return Optional.empty();
            else
                return Optional.of(entity);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    protected static Utilizator extractUser(ResultSet resultSet) throws SQLException {
        String username= resultSet.getString("username");
        String firstName=resultSet.getString("first_name");
        String lastName=resultSet.getString("last_name");
        String password = resultSet.getString("password");
        Utilizator user=new Utilizator(firstName,lastName, username, password);
        user.setId(username);
        return user;
    }
}
