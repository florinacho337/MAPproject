package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.repository.InMemoryRepository;
import ro.ubbcluj.map.repository.UserDBRepository;

import java.util.List;

public class UsersService implements Service<Long, Utilizator> {
//    InMemoryRepository<Long, Utilizator> repoUsers;
    UserDBRepository repoUsers;
    public UsersService(UserDBRepository repoUsers) {
        this.repoUsers = repoUsers;
    }

    @Override
    public void add(Utilizator E) {
        repoUsers.save(E);
    }

    @Override
    public Utilizator remove(Long id) {
        Utilizator u = null;
        List<Utilizator> friends;
        if(repoUsers.findOne(id).isPresent())
            u = repoUsers.findOne(id).get();
        if(u != null) {
            friends = u.getFriends();
            Utilizator finalU = u;
            friends.forEach(friend -> {
                friend.removeFriend(finalU);
            });
            u.removeAllFriends();
        }
        return repoUsers.delete(id).get();
    }

    @Override
    public Utilizator find(Long id) {
        return repoUsers.findOne(id).get();
    }

    @Override
    public Iterable<Utilizator> getAll() {
        return repoUsers.findAll();
    }
}
