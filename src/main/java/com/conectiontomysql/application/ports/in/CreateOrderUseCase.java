package com.conectiontomysql.application.ports.in;

import java.util.List;

import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Pedido;

public interface CreateOrderUseCase {
    void create(Pedido pedido, List<DetallePedido> details);
}
