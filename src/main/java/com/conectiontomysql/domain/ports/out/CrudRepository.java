package com.conectiontomysql.domain.ports.out;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    void save(T entity);
    List<T> findAll();
    Optional<T> findById(ID id);
    void update(T entity);
    boolean deleteById(ID id);
}
