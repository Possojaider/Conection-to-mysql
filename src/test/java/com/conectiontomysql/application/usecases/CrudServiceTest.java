package com.conectiontomysql.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.conectiontomysql.domain.model.Oficina;
import com.conectiontomysql.domain.ports.out.CrudRepository;

class CrudServiceTest {
    @Test
    void delegatesCrudOperationsToRepository() {
        InMemoryOfficeRepository repository = new InMemoryOfficeRepository();
        CrudService<Oficina, String> service = new CrudService<>(repository);
        Oficina office = new Oficina("MAD", "Madrid", "España", null, "28001",
                "910000000", "Calle 1", null);

        service.save(office);
        assertEquals(List.of(office), service.findAll());
        assertEquals(Optional.of(office), service.findById("MAD"));
        assertTrue(service.deleteById("MAD"));
        assertTrue(service.findAll().isEmpty());
    }

    private static final class InMemoryOfficeRepository implements CrudRepository<Oficina, String> {
        private final List<Oficina> records = new ArrayList<>();

        @Override
        public void save(Oficina entity) {
            records.add(entity);
        }

        @Override
        public List<Oficina> findAll() {
            return List.copyOf(records);
        }

        @Override
        public Optional<Oficina> findById(String id) {
            return records.stream().filter(record -> record.codigoOficina().equals(id)).findFirst();
        }

        @Override
        public void update(Oficina entity) {
            deleteById(entity.codigoOficina());
            save(entity);
        }

        @Override
        public boolean deleteById(String id) {
            return records.removeIf(record -> record.codigoOficina().equals(id));
        }
    }
}
