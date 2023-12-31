package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Tuple;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.PrietenieValidator;
import ro.ubbcluj.map.domain.validators.Validator;
import ro.ubbcluj.map.repository.Repository;
import ro.ubbcluj.map.utils.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FriendshipDBRepository implements Repository<Tuple<String, String>, Prietenie> {

    protected final Connection connection;
    private final Validator<Prietenie> validator;

    public FriendshipDBRepository(Connection connection) {
        this.connection = connection;
        validator = new PrietenieValidator();
    }

    @Override
    public Optional<Prietenie> findOne(Tuple<String, String> ID) {
        try(PreparedStatement statement = connection.prepareStatement("""
                    select "friendsFrom", u1.first_name as "firstNameU1", u1.last_name as "lastNameU1", u1.password as "passwordU1", u2.first_name as "firstNameU2", u2.last_name as "lastNameU2", u2.password as "passwordU2"\s
                    FROM friendships f INNER JOIN users u1 on u1.username = f.username1\s
                    INNER JOIN users u2 on u2.username = f.username2 where f.username1 = ? and f.username2 = ?""")
        ){
            statement.setString(1, ID.getLeft());
            statement.setString(2, ID.getRight());
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                String firstNameU1 = resultSet.getString("firstNameU1");
                String lastNameU1 = resultSet.getString("lastNameU1");
                String passwordU1 = resultSet.getString("passwordU1");
                String firstNameU2 = resultSet.getString("firstNameU2");
                String lastNameU2 = resultSet.getString("lastNameU2");
                String passwordU2 = resultSet.getString("passwordU2");
                String friendsFrom = resultSet.getString("friendsFrom");
                Utilizator u1 = new Utilizator(firstNameU1, lastNameU1, ID.getLeft(), passwordU1);
                Utilizator u2 = new Utilizator(firstNameU2, lastNameU2, ID.getRight(), passwordU2);
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

        try (PreparedStatement statement = connection.prepareStatement("""
                     select username1, username2, "friendsFrom", u1.first_name as "firstNameU1", u1.last_name as "lastNameU1", u1.password as "passwordU1", u2.first_name as "firstNameU2", u2.last_name as "lastNameU2", u2.password as "passwordU2" from friendships f
                     inner join users u1 on u1.username = f.username1
                     inner join users u2 on u2.username = f.username2""");
             ResultSet resultSet = statement.executeQuery()
        ) {

            extractResult(prietenii, resultSet);
            return prietenii;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        validator.validate(entity);
        try(PreparedStatement statement = connection.prepareStatement("insert into friendships " +
                    "values (?, ?, ?)")
        ){
            statement.setString(1, entity.getId().getLeft());
            statement.setString(2, entity.getId().getRight());
            statement.setString(3, entity.getDate().format(Constants.DATE_TIME_FORMATTER));
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
    public Optional<Prietenie> delete(Tuple<String, String> ID) {
        try(PreparedStatement statement = connection.prepareStatement("delete from friendships where username1 = ? and username2 = ?")
        ){
            statement.setString(1, ID.getLeft());
            statement.setString(2, ID.getRight());
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

    protected static void extractResult(Set<Prietenie> prietenii, ResultSet resultSet) throws SQLException {
        while (resultSet.next())
        {
            String idU1 = resultSet.getString("username1");
            String idU2 = resultSet.getString("username2");
            String friendsFrom = resultSet.getString("friendsFrom");
            String fristNameU1 = resultSet.getString("firstNameU1");
            String fristNameU2 = resultSet.getString("firstNameU2");
            String lastNameU1 = resultSet.getString("lastNameU1");
            String lastNameU2 = resultSet.getString("lastNameU2");
            String passwordU1 = resultSet.getString("passwordU1");
            String passwordU2 = resultSet.getString("passwordU2");
            Utilizator u1 = new Utilizator(fristNameU1, lastNameU1, idU1, passwordU1);
            Utilizator u2 = new Utilizator(fristNameU2, lastNameU2, idU2, passwordU2);
            u1.setId(idU1);
            u2.setId(idU2);
            Prietenie prietenie = new Prietenie(u1, u2, friendsFrom);
            Tuple<String, String> id = new Tuple<>(idU1, idU2);
            prietenie.setId(id);
            prietenii.add(prietenie);
        }
    }
}
