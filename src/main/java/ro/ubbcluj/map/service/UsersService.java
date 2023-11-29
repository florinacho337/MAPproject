package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.utils.events.ChangeEventType;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.observer.Observable;
import ro.ubbcluj.map.utils.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsersService implements Observable<UtilizatorChangeEvent>, Service<Long, Utilizator> {
    //    InMemoryRepository<Long, Utilizator> repoUsers;
    private final UserDBRepository repoUsers;
    private final List<Observer<UtilizatorChangeEvent>> observers = new ArrayList<>();

    public UsersService(UserDBRepository repoUsers) {
        this.repoUsers = repoUsers;
    }

    @Override
    public Utilizator add(Utilizator E) {
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
}
