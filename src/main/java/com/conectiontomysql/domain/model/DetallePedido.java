package com.conectiontomysql.domain.model;

import java.math.BigDecimal;

public record DetallePedido(
        Integer codigoPedido,
        String codigoProducto,
        Integer cantidad,
        BigDecimal precioUnidad,
        Integer numeroLinea
) {
    public DetallePedido {
        if (codigoPedido == null || codigoPedido <= 0) {
            throw new IllegalArgumentException("El código del pedido debe ser positivo.");
        }
        if (codigoProducto == null || codigoProducto.isBlank()) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva.");
        }
        if (precioUnidad == null || precioUnidad.signum() < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser cero o mayor.");
        }
        if (numeroLinea == null || numeroLinea <= 0) {
            throw new IllegalArgumentException("El número de línea debe ser positivo.");
        }
    }
}
