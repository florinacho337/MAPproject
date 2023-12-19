package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.entities.Message;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.repository.dbrepositories.MessageDBRepository;
import ro.ubbcluj.map.repository.paging.Page;
import ro.ubbcluj.map.repository.paging.Pageable;
import ro.ubbcluj.map.repository.paging.PageableImplementation;
import ro.ubbcluj.map.repository.pagingrepositories.UserDBPagingRepository;
import ro.ubbcluj.map.utils.events.ChangeEventType;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.observer.Observable;
import ro.ubbcluj.map.utils.observer.Observer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UsersService implements Observable<UtilizatorChangeEvent>, Service<String, Utilizator> {
    //    InMemoryRepository<Long, Utilizator> repoUsers;
//    private final UserDBRepository repoUsers;
    private final MessageDBRepository repoMessages;
    private final UserDBPagingRepository repoUsers;
    private final List<Observer<UtilizatorChangeEvent>> observers = new ArrayList<>();
    private int page;
    private int pageSize;
    private Pageable pageable;

    public UsersService(UserDBPagingRepository repoUsers, MessageDBRepository repoMessages) {
        this.repoMessages = repoMessages;
        this.repoUsers = repoUsers;
    }

    @Override
    public Utilizator add(Utilizator E) {
        Optional<Utilizator> user = repoUsers.save(E);
        if (user.isPresent()) {
            return E;
        }
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD, null));
        return null;
    }

    @Override
    public Utilizator remove(String id) {
        Optional<Utilizator> user = repoUsers.delete(id);
        if (user.isPresent()) {
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.DELETE, user.get()));
            return user.get();
        }
        return null;
    }

    @Override
    public Utilizator find(String id) {
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

    public void sendMessage(Utilizator from, List<Utilizator> to, String content) {
        repoMessages.save(new Message(from, to, content, null));
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD, null));
    }

    public void replyMessage(Utilizator from, Utilizator to, Message replyTo, String content) {
        repoMessages.save(new Message(from, Collections.singletonList(to), content, replyTo));
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD, null));
    }

    public Iterable<Message> getMessages(Utilizator u1, Utilizator u2) {
        Iterable<Message> messages = repoMessages.findAll();
        return StreamSupport.stream(messages.spliterator(), false)
                .filter(message -> {
                    if (Objects.equals(message.getFrom().getId(), u1.getId()) &&
                            !message.getTo().stream()
                                    .filter(utilizator -> Objects.equals(utilizator.getId(), u2.getId()))
                                    .toList()
                                    .isEmpty())
                        return true;
                    return Objects.equals(message.getFrom().getId(), u2.getId()) &&
                            !message.getTo().stream()
                                    .filter(utilizator -> Objects.equals(utilizator.getId(), u1.getId()))
                                    .toList()
                                    .isEmpty();
                }).toList();
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

    public void setPageSize(int size) {
        this.pageSize = size;
    }

//    public void setPageable(Pageable pageable) {
//        this.pageable = pageable;
//    }

//    public Set<Utilizator> getNextUsers() {
//        this.page++;
//        return getUsersOnPage(this.page);
//    }

    public Set<Utilizator> getUsersOnPage(int page, Utilizator utilizator) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.pageSize);
        Page<Utilizator> userPage = repoUsers.findAll(pageable, utilizator.getId());
        return userPage.getContent().collect(Collectors.toSet());
    }
}
