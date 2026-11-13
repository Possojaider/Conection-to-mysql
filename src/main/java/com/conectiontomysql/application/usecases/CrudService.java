package com.conectiontomysql.application.usecases;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.conectiontomysql.application.ports.in.CrudUseCase;
import com.conectiontomysql.domain.ports.out.CrudRepository;

public final class CrudService<T, ID> implements CrudUseCase<T, ID> {
    private final CrudRepository<T, ID> repository;

    public CrudService(CrudRepository<T, ID> repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void save(T entity) {
        repository.save(Objects.requireNonNull(entity));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(repository.findAll());
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(Objects.requireNonNull(id));
    }

    @Override
    public void update(T entity) {
        repository.update(Objects.requireNonNull(entity));
    }

    @Override
    public boolean deleteById(ID id) {
        return repository.deleteById(Objects.requireNonNull(id));
    }
}
