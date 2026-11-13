package com.conectiontomysql.domain.model;

import java.time.LocalDate;

public record Pedido(
        Integer codigoPedido,
        LocalDate fechaPedido,
        LocalDate fechaEsperada,
        LocalDate fechaEntrega,
        String estado,
        String comentarios,
        Integer codigoCliente
) {
    public Pedido {
        if (codigoPedido == null || codigoPedido <= 0) {
            throw new IllegalArgumentException("El código del pedido debe ser positivo.");
        }
        if (fechaPedido == null || fechaEsperada == null) {
            throw new IllegalArgumentException("Las fechas del pedido y de entrega esperada son obligatorias.");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio.");
        }
        if (codigoCliente == null || codigoCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente debe ser positivo.");
        }
    }
}
