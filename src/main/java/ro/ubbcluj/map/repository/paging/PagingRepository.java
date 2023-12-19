package ro.ubbcluj.map.repository.paging;

import ro.ubbcluj.map.domain.entities.Entity;
import ro.ubbcluj.map.repository.Repository;

public interface PagingRepository<ID,
        E extends Entity<ID>>
        extends Repository<ID, E> {

    Page<E> findAll(Pageable pageable, String id);
}
