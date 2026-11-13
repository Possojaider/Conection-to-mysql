package com.conectiontomysql.domain.model;

public record GamaProducto(
        String gama,
        String descripcionTexto,
        String descripcionHtml,
        String imagen
) {
    public GamaProducto {
        if (gama == null || gama.isBlank()) {
            throw new IllegalArgumentException("El nombre de la gama es obligatorio.");
        }
    }
}
