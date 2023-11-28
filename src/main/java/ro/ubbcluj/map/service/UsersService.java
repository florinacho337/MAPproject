package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.entities.*;
import ro.ubbcluj.map.repository.InMemoryRepository;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.utils.events.ChangeEventType;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.observer.Observable;
import ro.ubbcluj.map.utils.observer.Observer;

import java.util.*;

public class UsersService implements Observable<UtilizatorChangeEvent>, Service<Long, Utilizator> {
//        InMemoryRepository<Long, Utilizator> repoUsers;
        InMemoryRepository<Long, Message> repoMessages;
    private final UserDBRepository repoUsers;
    private final List<Observer<UtilizatorChangeEvent>> observers = new ArrayList<>();

    public UsersService(UserDBRepository repoUsers) {
        this.repoUsers = repoUsers;
//        this.repoMessages = repoMessages;
    }
    
    @Override
    public Utilizator add(Utilizator E) {
//        setID(repoUsers, E);
        Optional<Utilizator> user = repoUsers.save(E);
        if(user.isPresent()){
            return E;
        }
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD, null));
        return null;
    }

    @Override
    public Utilizator remove(Long id) {
        Optional<Utilizator> user = repoUsers.delete(id);
        if (user.isPresent()) {
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.DELETE, user.get()));
            return user.get();
        }
        return null;
    }

    @Override
    public Utilizator find(Long id) {
        Optional<Utilizator> user = repoUsers.findOne(id);
        return user.orElse(null);
    }

    @Override
    public Utilizator update(Utilizator entity) {
        Optional<Utilizator> user = repoUsers.findOne(entity.getId());
        if (user.isPresent()) {
            repoUsers.update(entity);
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.UPDATE, entity, user.get()));
            return null;
        }
        return entity;
    }

    @Override
    public Iterable<Utilizator> getAll() {
        return repoUsers.findAll();
    }

    @Override
    public void addObserver(Observer<UtilizatorChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<UtilizatorChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UtilizatorChangeEvent t) {
        observers.forEach(x -> x.update(t));
    }
    
    public Message sendMessage(Utilizator from, String to, String mesaj) {
        List<Utilizator> users_to = Arrays.stream(to.split(",")).map(id_user -> find(Long.valueOf(id_user))).toList();
        Message message = new Message(from, users_to, mesaj);
        setID(repoMessages, message);
        Optional<Message> messageOptional = repoMessages.save(message);
        return messageOptional.orElse(null);
    }

    private Message findMessage(Long id_message){
        Optional<Message> messages = repoMessages.findOne(id_message);
        return messages.orElse(null);
    }
//    public Message replyToMessage(Long id_message, Utilizator from, String mesaj){
//        Message mesajPrimit = findMessage(id_message);
//        Message message = new ReplyMessage(from, Collections.singletonList(mesajPrimit.getFrom()), mesaj, mesajPrimit);
//
//    }
    private <T extends Entity<Long>> void setID(InMemoryRepository<Long, T> repository, T entity) {
        long id = 0;
        while(repository.findOne(id).isPresent())
            id++;
        entity.setId(id);
    }
}
