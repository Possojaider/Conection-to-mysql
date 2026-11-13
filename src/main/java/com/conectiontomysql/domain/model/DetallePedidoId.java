package com.conectiontomysql.domain.model;

public record DetallePedidoId(Integer codigoPedido, String codigoProducto) {
    public DetallePedidoId {
        if (codigoPedido == null || codigoProducto == null || codigoProducto.isBlank()) {
            throw new IllegalArgumentException("La clave del detalle debe incluir pedido y producto.");
        }
    }
}
