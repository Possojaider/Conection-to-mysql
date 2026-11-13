package com.conectiontomysql.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Pago(
        Integer codigoCliente,
        String formaPago,
        String idTransaccion,
        LocalDate fechaPago,
        BigDecimal total
) {
    public Pago {
        if (codigoCliente == null || codigoCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente debe ser positivo.");
        }
        if (formaPago == null || formaPago.isBlank()) {
            throw new IllegalArgumentException("La forma de pago es obligatoria.");
        }
        if (idTransaccion == null || idTransaccion.isBlank()) {
            throw new IllegalArgumentException("El identificador de transacción es obligatorio.");
        }
        if (fechaPago == null) {
            throw new IllegalArgumentException("La fecha de pago es obligatoria.");
        }
        if (total == null || total.signum() < 0) {
            throw new IllegalArgumentException("El total del pago debe ser cero o mayor.");
        }
    }
}
