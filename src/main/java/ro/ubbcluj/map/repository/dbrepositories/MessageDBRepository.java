package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.Message;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.MessageValidator;
import ro.ubbcluj.map.repository.Repository;
import ro.ubbcluj.map.utils.Constants;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository implements Repository<Long, Message> {
    protected final String url;
    protected final String username;
    protected final String password;
    private final MessageValidator validator;

    public MessageDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = new MessageValidator();
    }

    @Override
    public Optional<Message> findOne(Long aLong) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("""
                     select "from", u1.first_name as "first_name_from", u1.last_name as "last_name_from", u1.password as "password_u1",  "to", u2.first_name as "first_name_to", u2.last_name as "last_name_to", u2.password as "password_u2", message, data, reply_to
                     from conversations inner join messages on conversations.id_message = messages.id
                     inner join users u1 on conversations."from" = u1.username
                     inner join users u2 on conversations."to" = u2.username
                     where id_message = ?""")
        ) {
            statement.setInt(1, Math.toIntExact(aLong));
            ResultSet resultSet = statement.executeQuery();
            Message newMessage = createMessage(aLong, resultSet);
            if(newMessage != null)
                return Optional.of(newMessage);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Message createMessage(Long aLong, ResultSet resultSet) throws SQLException {
        List<Utilizator> to = new ArrayList<>();
        String message = null;
        LocalDateTime data = null;
        Utilizator from = null;
        Message replyTo = null;
        while (resultSet.next()){
            if(to.isEmpty()){
                data = LocalDateTime.parse(resultSet.getString("data"), Constants.DATE_TIME_FORMATTER);
                message = resultSet.getString("message");
                from = new Utilizator(resultSet.getString("first_name_from"), resultSet.getString("last_name_from"), resultSet.getString("from"), resultSet.getString("password_u1"));
                from.setId(resultSet.getString("from"));
                Long idReply = resultSet.getLong("reply_to");
                if(idReply != 1 &&  findOne(idReply).isPresent())
                    replyTo = findOne(idReply).get();
            }
            Utilizator userTo = new Utilizator(resultSet.getString("first_name_to"), resultSet.getString("last_name_to"), resultSet.getString("to"), resultSet.getString("password_u2"));
            userTo.setId(resultSet.getString("to"));
            to.add(userTo);
        }
        if(message != null && !to.isEmpty()) {
            Message newMessage = new Message(from, to, message, data, replyTo);
            newMessage.setId(aLong);
            return newMessage;
        }
        return null;
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select id from messages");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next())
            {
                Optional<Message> message = findOne(resultSet.getLong("id"));
                message.ifPresent(messages::add);
            }
            return messages;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> save(Message entity) {
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("""
                    insert into messages(message, data, reply_to)
                    values (?, ?, ?)""")
        ){
            statement.setString(1, entity.getContent());
            statement.setString(2, entity.getData().format(Constants.DATE_TIME_FORMATTER));
            if(entity.getReplyTo() == null)
                statement.setInt(3, 1);
            else
                statement.setInt(3, Math.toIntExact(entity.getReplyTo().getId()));
            int response = statement.executeUpdate();
            if(response != 0) {
                insertConversations(entity, connection);
                return Optional.empty();
            }else
                return Optional.of(entity);
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    private static void insertConversations(Message entity, Connection connection) {
        try(PreparedStatement selectMessage = connection.prepareStatement("select max(id) as \"id\" from messages");
            ResultSet resultSet = selectMessage.executeQuery()
        ){
            if(resultSet.next()) {
                entity.getTo().forEach(utilizator -> insertConversation(entity, connection, utilizator, resultSet));
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    private static void insertConversation(Message entity, Connection connection, Utilizator utilizator, ResultSet resultSet) {
        try (PreparedStatement adauga_conversatie = connection.prepareStatement("insert into conversations(id_message, \"from\", \"to\")\n" +
                "values (?, ?, ?)")) {
            adauga_conversatie.setInt(1, Math.toIntExact(resultSet.getLong("id")));
            adauga_conversatie.setString(2, entity.getFrom().getId());
            adauga_conversatie.setString(3, utilizator.getId());
            adauga_conversatie.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }
}
