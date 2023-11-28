package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.UtilizatorValidator;
import ro.ubbcluj.map.domain.validators.Validator;
import ro.ubbcluj.map.repository.Repository;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDBRepository implements Repository<Long, Utilizator> {

    private final String url;
    private final String username;
    private final String password;
    private final Validator<Utilizator> validator;

    public UserDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        validator = new UtilizatorValidator();
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
    public Optional<Utilizator> findOne(Long longID) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select * from users " +
                    "where id = ?")

        ) {
            statement.setInt(1, Math.toIntExact(longID));
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                Utilizator u = new Utilizator(firstName,lastName);
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
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next())
            {
                Long id= resultSet.getLong("id");
                String firstName=resultSet.getString("first_name");
                String lastName=resultSet.getString("last_name");
                Utilizator user=new Utilizator(firstName,lastName);
                user.setId(id);
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
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into users(first_name, last_name) " +
                    "values (?, ?)")
        ){
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
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
    public Optional<Utilizator> delete(Long ID) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from users where id = ?")
        ){
            statement.setInt(1, Math.toIntExact(ID));
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
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("update users set first_name = ?, last_name = ? where id = ?")
        ){
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setInt(3, Math.toIntExact(entity.getId()));
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
