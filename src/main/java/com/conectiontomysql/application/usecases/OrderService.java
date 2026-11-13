package com.conectiontomysql.application.usecases;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.conectiontomysql.application.ports.in.CreateOrderUseCase;
import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Pedido;
import com.conectiontomysql.domain.ports.out.OrderTransactionPort;

public final class OrderService implements CreateOrderUseCase {
    private final OrderTransactionPort orderTransactionPort;

    public OrderService(OrderTransactionPort orderTransactionPort) {
        this.orderTransactionPort = Objects.requireNonNull(orderTransactionPort);
    }

    @Override
    public void create(Pedido pedido, List<DetallePedido> details) {
        Objects.requireNonNull(pedido, "El pedido es obligatorio.");
        if (pedido.fechaEsperada().isBefore(pedido.fechaPedido())
                || pedido.fechaEntrega() != null && pedido.fechaEntrega().isBefore(pedido.fechaPedido())) {
            throw new IllegalArgumentException("Las fechas esperada y de entrega no pueden ser anteriores al pedido.");
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Un pedido debe tener al menos un detalle.");
        }
        Set<String> products = new HashSet<>();
        for (DetallePedido detail : details) {
            if (!detail.codigoPedido().equals(pedido.codigoPedido())) {
                throw new IllegalArgumentException("Todos los detalles deben pertenecer al pedido.");
            }
            if (!products.add(detail.codigoProducto())) {
                throw new IllegalArgumentException("Un producto no puede repetirse en el mismo pedido.");
            }
        }
        orderTransactionPort.createAtomically(pedido, List.copyOf(details));
    }
}
