package com.conectiontomysql.domain.model;

public record PagoId(Integer codigoCliente, String idTransaccion) {
    public PagoId {
        if (codigoCliente == null || idTransaccion == null || idTransaccion.isBlank()) {
            throw new IllegalArgumentException("La clave del pago debe incluir cliente y transacción.");
        }
    }
}
