package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.Entity;
import ro.ubbcluj.map.domain.validators.ValidationException;

public interface Service<ID, E extends Entity<ID>> {
    /**
     * adds a new entity
     *
     * @param entity entity must not be null
     * @throws ValidationException if the entity is not valid
     * @throws IllegalArgumentException if the given entity is null.
     */
    E add(E entity);

    /**
     * removes the entity with the specified id
     *
     * @param id id must be not null
     * @return the removed entity
     * @throws IllegalArgumentException if the given id is null.
     */
    E remove(ID id);

    /**
     * @param id the id o the entity to be returned
     *           id must be not null
     * @return the entity with the given id
     * @throws IllegalArgumentException if id is null.
     */
    E find(ID id);

    E update(E entity);

    /**
     * @return all entities
     */
    Iterable<E> getAll();
}
