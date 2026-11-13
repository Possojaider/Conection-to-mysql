package com.conectiontomysql.domain.ports.out;

import java.util.List;

import com.conectiontomysql.domain.model.Pedido;

public interface PedidoRepository extends CrudRepository<Pedido, Integer> {
    List<Pedido> findByStatus(String status);
}
