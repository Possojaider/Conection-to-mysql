package com.conectiontomysql.domain.ports.out;

import java.util.Optional;

import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.DetallePedidoId;

public interface DetallePedidoRepository extends CrudRepository<DetallePedido, DetallePedidoId> {
    Optional<DetallePedido> findById(Integer codigoPedido, String codigoProducto);
    boolean deleteById(Integer codigoPedido, String codigoProducto);

    @Override
    default Optional<DetallePedido> findById(DetallePedidoId id) {
        return findById(id.codigoPedido(), id.codigoProducto());
    }

    @Override
    default boolean deleteById(DetallePedidoId id) {
        return deleteById(id.codigoPedido(), id.codigoProducto());
    }
}
