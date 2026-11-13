package com.conectiontomysql.domain.model;

import java.math.BigDecimal;

public record Cliente(
        Integer codigoCliente,
        String nombreCliente,
        String nombreContacto,
        String apellidoContacto,
        String telefono,
        String fax,
        String lineaDireccion1,
        String lineaDireccion2,
        String ciudad,
        String region,
        String pais,
        String codigoPostal,
        Integer codigoEmpleadoRepVentas,
        BigDecimal limiteCredito,
        boolean activo
) {
    public Cliente {
        if (codigoCliente == null || codigoCliente <= 0) {
            throw new IllegalArgumentException("El código del cliente debe ser positivo.");
        }
        if (nombreCliente == null || nombreCliente.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (telefono == null || telefono.isBlank() || fax == null || fax.isBlank()) {
            throw new IllegalArgumentException("El teléfono y fax del cliente son obligatorios.");
        }
        if (lineaDireccion1 == null || lineaDireccion1.isBlank()) {
            throw new IllegalArgumentException("La dirección del cliente es obligatoria.");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad del cliente es obligatoria.");
        }
        if (limiteCredito != null && limiteCredito.signum() < 0) {
            throw new IllegalArgumentException("El límite de crédito no puede ser negativo.");
        }
    }
}
