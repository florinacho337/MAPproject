//package ro.ubbcluj.map.repository.dbrepositories;

//import ro.ubbcluj.map.domain.*;
//import ro.ubbcluj.map.repository.Repository;
//
//import java.sql.*;
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;

//public class MessageDBRepository implements Repository<Long, Message> {
//    private final String url;
//    private final String username;
//    private final String password;
//
//    public MessageDBRepository(String url, String username, String password) {
//        this.url = url;
//        this.username = username;
//        this.password = password;
//    }
//
//    @Override
//    public Optional<Message> findOne(Long aLong) {
//        return Optional.empty();
//    }
//
//    @Override
//    public Iterable<Message> findAll() {
//        Set<Message> mesaje = new HashSet<>();
//
//        try (Connection connection = DriverManager.getConnection(url, username, password);
//             PreparedStatement statement = connection.prepareStatement("select m.id, \"from\", \"to\", mesaj, data, \"replyTo\", u1.first_name as \"firstNameU1\", u1.last_name as \"lastNameU1\", u2.first_name as \"firstNameU2\", u2.last_name as \"lastNameU2\" from messages m\n" +
//                     "inner join users u1 on u1.id = m.from\n" +
//                     "inner join users u2 on u2.id = m.to\n");
//             ResultSet resultSet = statement.executeQuery()
//        ) {
//
//            while (resultSet.next())
//            {
//                Long id = resultSet.getLong("id");
//                Long from = resultSet.getLong("from");
//                Long to = resultSet.getLong("to");
//                String mesaj = resultSet.getString("mesaj");
//                String data = resultSet.getString("data");
//                Long replyTo = resultSet.getLong("replyTo");
//                String fristNameU1 = resultSet.getString("firstNameU1");
//                String fristNameU2 = resultSet.getString("firstNameU2");
//                String lastNameU1 = resultSet.getString("lastNameU1");
//                String lastNameU2 = resultSet.getString("lastNameU2");
//                Utilizator u1 = new Utilizator(fristNameU1, lastNameU1);
//                Utilizator u2 = new Utilizator(fristNameU2, lastNameU2);
//                u1.setId(from);
//                u2.setId(to);
//                Message message = null;
//                if(replyTo == -1)
//                    message = new Message(u1, u2, mesaj);
//                else {
//                    if (findOne(replyTo).isPresent())
//                        message = new ReplyMessage(u1, u2, mesaj, findOne(replyTo).get());
//                }
//                Objects.requireNonNull(message).setId(id);
//                mesaje.add(message);
//            }
//            return mesaje;
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public Optional<Message> save(Message entity) {
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<Message> delete(Long aLong) {
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<Message> update(Message entity) {
//        return Optional.empty();
//    }
//}
