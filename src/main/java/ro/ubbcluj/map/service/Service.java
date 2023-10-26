package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.Entity;

public interface Service<ID, E extends Entity<ID>> {
    void add(E entity);

    E remove(ID id);

    E find(ID id);
}
