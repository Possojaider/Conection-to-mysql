package com.conectiontomysql.domain.ports.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.domain.model.Pago;
import com.conectiontomysql.domain.model.PagoId;

public interface PagoRepository extends CrudRepository<Pago, PagoId> {
    Optional<Pago> findById(Integer codigoCliente, String idTransaccion);
    boolean deleteById(Integer codigoCliente, String idTransaccion);
    List<Pago> findByDateRange(LocalDate start, LocalDate end);

    @Override
    default Optional<Pago> findById(PagoId id) {
        return findById(id.codigoCliente(), id.idTransaccion());
    }

    @Override
    default boolean deleteById(PagoId id) {
        return deleteById(id.codigoCliente(), id.idTransaccion());
    }
}
