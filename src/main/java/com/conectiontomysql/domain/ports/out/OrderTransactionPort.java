package com.conectiontomysql.domain.ports.out;

import java.util.List;

import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Pedido;

public interface OrderTransactionPort {
    void createAtomically(Pedido order, List<DetallePedido> details);
}
