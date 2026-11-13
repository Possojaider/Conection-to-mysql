package com.conectiontomysql.domain.model;

import java.math.BigDecimal;

public record Producto(
        String codigoProducto,
        String nombre,
        String gama,
        String dimensiones,
        String proveedor,
        String descripcion,
        Integer cantidadEnStock,
        BigDecimal precioVenta,
        BigDecimal precioProveedor
) {
    public Producto {
        if (codigoProducto == null || codigoProducto.isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (gama == null || gama.isBlank()) {
            throw new IllegalArgumentException("La gama del producto es obligatoria.");
        }
        if (cantidadEnStock == null || cantidadEnStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (precioVenta == null || precioVenta.signum() < 0) {
            throw new IllegalArgumentException("El precio de venta debe ser cero o mayor.");
        }
        if (precioProveedor != null && precioProveedor.signum() < 0) {
            throw new IllegalArgumentException("El precio de proveedor no puede ser negativo.");
        }
    }
}
