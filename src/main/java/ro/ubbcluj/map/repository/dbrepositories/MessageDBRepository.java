package ro.ubbcluj.map.repository.dbrepositories;

import ro.ubbcluj.map.domain.entities.Entity;
import ro.ubbcluj.map.domain.entities.Message;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.domain.validators.MessageValidator;
import ro.ubbcluj.map.repository.Repository;
import ro.ubbcluj.map.utils.Constants;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MessageDBRepository implements Repository<Long, Message> {
    private final String url;
    private final String username;
    private final String password;
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
                     select "from", u.first_name as from_first_name, u.last_name as from_last_name, "to", mesaj, data, reply_to
                     from users u inner join messages m on m."from" = u.id
                     where m.id = ?""")
        ) {
            statement.setInt(1, Math.toIntExact(aLong));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                Message message = extractMessage(aLong, resultSet);
                return Optional.of(message);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Message extractMessage(Long aLong, ResultSet resultSet) throws SQLException {
        Long idFrom = resultSet.getLong("from");
        String fromFirstName = resultSet.getString("from_first_name");
        String fromLastName = resultSet.getString("from_last_name");
        String stringTo = resultSet.getString("to");
        String content = resultSet.getString("mesaj");
        String data = resultSet.getString("data");
        long replyTo = resultSet.getLong("reply_to");
        Optional<Message> messageOptional;
        if(replyTo != -1)
            messageOptional = findOne(replyTo);
        else
            messageOptional = Optional.empty();
        Utilizator from = new Utilizator(fromFirstName, fromLastName);
        from.setId(idFrom);
        List<Utilizator> to = Arrays.stream(stringTo.split(","))
                .map(x -> {Utilizator u = new Utilizator(); u.setId(Long.valueOf(x)); return u;})
                .toList();
        Message message = messageOptional.map(value -> new Message(from, to, content, LocalDateTime.parse(data, Constants.DATE_TIME_FORMATTER), value))
                .orElseGet(() -> new Message(from, to, content, LocalDateTime.parse(data, Constants.DATE_TIME_FORMATTER), null));
        message.setId(aLong);
        return message;
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("""
                     select m.id, "from", u.first_name as from_first_name, u.last_name as from_last_name, "to", mesaj, data, reply_to
                     from users u inner join messages m on m."from" = u.id""");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                messages.add(extractMessage(id, resultSet));
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
            PreparedStatement statement = connection.prepareStatement("insert into messages(\"from\", \"to\", mesaj, data, reply_to) " +
                    "values (?, ?, ?, ?, ?)")
        ){
            String to = entity.getTo().stream()
                    .map(utilizator -> String.valueOf(utilizator.getId()))
                    .collect(Collectors.joining(","));
            statement.setInt(1, Math.toIntExact(entity.getFrom().getId()));
            statement.setString(2, to);
            statement.setString(3, entity.getContent());
            statement.setString(4, entity.getData().format(Constants.DATE_TIME_FORMATTER));
            if(entity.getReplyTo() == null)
                statement.setInt(5, -1);
            else
                statement.setInt(5, Math.toIntExact(entity.getReplyTo().getId()));
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
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }
}
