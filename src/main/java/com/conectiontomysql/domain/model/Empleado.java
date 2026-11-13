package com.conectiontomysql.domain.model;

public record Empleado(
        Integer codigoEmpleado,
        String nombre,
        String apellido1,
        String apellido2,
        String extension,
        String email,
        String codigoOficina,
        Integer codigoJefe,
        String puesto
) {
    public Empleado {
        if (codigoEmpleado == null || codigoEmpleado <= 0) {
            throw new IllegalArgumentException("El código del empleado debe ser positivo.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del empleado es obligatorio.");
        }
        if (apellido1 == null || apellido1.isBlank()) {
            throw new IllegalArgumentException("El primer apellido es obligatorio.");
        }
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("La extensión del empleado es obligatoria.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("El correo electrónico del empleado no es válido.");
        }
        if (codigoOficina == null || codigoOficina.isBlank()) {
            throw new IllegalArgumentException("La oficina del empleado es obligatoria.");
        }
    }
}
