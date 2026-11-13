package com.conectiontomysql.domain.model;

public record Oficina(
        String codigoOficina,
        String ciudad,
        String pais,
        String region,
        String codigoPostal,
        String telefono,
        String lineaDireccion1,
        String lineaDireccion2
) {
    public Oficina {
        if (codigoOficina == null || codigoOficina.isBlank()
                || ciudad == null || ciudad.isBlank()
                || pais == null || pais.isBlank()
                || codigoPostal == null || codigoPostal.isBlank()
                || telefono == null || telefono.isBlank()
                || lineaDireccion1 == null || lineaDireccion1.isBlank()) {
            throw new IllegalArgumentException("Código, ciudad, país, código postal, teléfono y dirección son obligatorios.");
        }
    }
}
