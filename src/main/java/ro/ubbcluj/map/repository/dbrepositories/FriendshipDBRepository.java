package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Tuple;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.PrietenieValidator;
import ro.ubbcluj.map.domain.validators.Validator;
import ro.ubbcluj.map.repository.Repository;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FriendshipDBRepository implements Repository<Tuple<Long, Long>, Prietenie> {

    private final String url;
    private final String username;
    private final String password;
    private final Validator<Prietenie> validator;

    public FriendshipDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        validator = new PrietenieValidator();
    }

    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> ID) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("select \"friendsFrom\", u1.first_name as \"firstNameU1\", u1.last_name as \"lastNameU1\"," +
                    "u2.first_name as \"firstNameU2\", u2.last_name as \"lastNameU2\" FROM friendships f INNER JOIN users u1 on u1.id = f.id_u1 " +
                    "INNER JOIN users u2 on u2.id = f.id_u2 where f.id_u1 = ? and f.id_u2 = ?")
        ){
            statement.setInt(1, Math.toIntExact(ID.getLeft()));
            statement.setInt(2, Math.toIntExact(ID.getRight()));
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                String firstNameU1 = resultSet.getString("firstNameU1");
                String lastNameU1 = resultSet.getString("lastNameU1");
                String firstNameU2 = resultSet.getString("firstNameU2");
                String lastNameU2 = resultSet.getString("lastNameU2");
                String friendsFrom = resultSet.getString("friendsFrom");
                Utilizator u1 = new Utilizator(firstNameU1, lastNameU1);
                Utilizator u2 = new Utilizator(firstNameU2, lastNameU2);
                u1.setId(ID.getLeft());
                u2.setId(ID.getRight());
                Prietenie prietenie = new Prietenie(u1, u2, friendsFrom);
                prietenie.setId(ID);
                return Optional.of(prietenie);
            }
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Prietenie> findAll() {
        Set<Prietenie> prietenii = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("""
                     select id_u1, id_u2, "friendsFrom", u1.first_name as "firstNameU1", u1.last_name as "lastNameU1", u2.first_name as "firstNameU2", u2.last_name as "lastNameU2" from friendships f
                     inner join users u1 on u1.id = f.id_u1
                     inner join users u2 on u2.id = f.id_u2""");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next())
            {
                Long idU1 = resultSet.getLong("id_u1");
                Long idU2 = resultSet.getLong("id_u2");
                String friendsFrom = resultSet.getString("friendsFrom");
                String fristNameU1 = resultSet.getString("firstNameU1");
                String fristNameU2 = resultSet.getString("firstNameU2");
                String lastNameU1 = resultSet.getString("lastNameU1");
                String lastNameU2 = resultSet.getString("lastNameU2");
                Utilizator u1 = new Utilizator(fristNameU1, lastNameU1);
                Utilizator u2 = new Utilizator(fristNameU2, lastNameU2);
                u1.setId(idU1);
                u2.setId(idU2);
                Prietenie prietenie = new Prietenie(u1, u2, friendsFrom);
                Tuple<Long, Long> id = new Tuple<>(idU1, idU2);
                prietenie.setId(id);
                prietenii.add(prietenie);
            }
            return prietenii;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("insert into friendships " +
                    "values (?, ?, ?)")
        ){
            statement.setInt(1, Math.toIntExact(entity.getId().getLeft()));
            statement.setInt(2, Math.toIntExact(entity.getId().getRight()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            statement.setString(3, entity.getDate().format(formatter));
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
    public Optional<Prietenie> delete(Tuple<Long, Long> ID) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("delete from friendships where id_u1 = ? and id_u2 = ?")
        ){
            statement.setInt(1, Math.toIntExact(ID.getLeft()));
            statement.setInt(2, Math.toIntExact(ID.getRight()));
            Optional<Prietenie> prietenie = findOne(ID);
            int response = statement.executeUpdate();
            if(response != 0)
                return prietenie;
            else
                return Optional.empty();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Prietenie> update(Prietenie entity) {return Optional.empty();}
}
