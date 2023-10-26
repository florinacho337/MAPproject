package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.repository.InMemoryRepository;

public class UsersService implements Service<Long, Utilizator>{
    InMemoryRepository<Long, Utilizator> repoUsers;

    public UsersService(InMemoryRepository<Long, Utilizator> repoUsers){
        this.repoUsers = repoUsers;
    }
    @Override
    public void add(Utilizator E) {
        long id = 0;
        while(repoUsers.findOne(id) != null)
            id++;
        E.setId(id);
        repoUsers.save(E);
    }

    @Override
    public Utilizator remove(Long id) {
        return repoUsers.delete(id);
    }

    @Override
    public Utilizator find(Long id) {
        return repoUsers.findOne(id);
    }
}
