package com.conectiontomysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class Conexion {

    private Conexion() {
    }

    public static Connection conectar() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    public static String diagnosticoError(Throwable error, String ubicacion) {
        Objects.requireNonNull(error, "El error es obligatorio.");
        Throwable original = error;
        SQLException sqlError = null;
        while (original.getCause() != null && original.getCause() != original) {
            if (sqlError == null && original instanceof SQLException sqlException) {
                sqlError = sqlException;
            }
            original = original.getCause();
        }
        if (sqlError == null && original instanceof SQLException sqlException) {
            sqlError = sqlException;
        }

        String tipo;
        String mensaje;
        String causa;
        String solucion;
        String sqlState = "No disponible";
        String codigoMySql = "No disponible";
        if (sqlError != null) {
            int code = sqlError.getErrorCode();
            sqlState = sqlError.getSQLState() == null ? "No disponible" : sqlError.getSQLState();
            codigoMySql = code == 0 ? "No disponible" : Integer.toString(code);
            mensaje = sqlError.getMessage();
            if (code == 1045 || "28000".equals(sqlState)) {
                tipo = "Credenciales o permisos MySQL";
                causa = "Usuario, contraseña o permisos de acceso incorrectos.";
                solucion = "Verifica DB_USER, DB_PASSWORD, los permisos del usuario y el host permitido en MySQL.";
            } else if (code == 1049 || "3D000".equals(sqlState)) {
                tipo = "Base de datos inexistente";
                causa = "El esquema indicado en la URL JDBC no existe.";
                solucion = "Crea jardineria o corrige el nombre de la base en DB_URL; revisa el script de inicialización.";
            } else if (code == 1146 || "42S02".equals(sqlState)) {
                tipo = "Tabla inexistente";
                causa = "La tabla no existe en el esquema conectado o falta una migración.";
                solucion = "Comprueba el esquema activo y aplica db/init/01-schema.sql o la migración correspondiente.";
            } else if (code == 1054 || "42S22".equals(sqlState)) {
                tipo = "Columna inexistente";
                causa = "El SQL y el esquema de la base no usan el mismo nombre de columna.";
                solucion = "Compara la consulta con la metadata real y actualiza el SQL o la migración.";
            } else if (code == 1062) {
                tipo = "Clave primaria o valor único duplicado";
                causa = "Ya existe un registro con la misma clave.";
                solucion = "Usa un identificador único o busca el registro existente antes de insertarlo.";
            } else if (code == 1451) {
                tipo = "Violación de clave foránea al eliminar";
                causa = "Hay registros dependientes que referencian este registro.";
                solucion = "Elimina o reasigna primero los registros dependientes; conserva la integridad referencial.";
            } else if (code == 1452) {
                tipo = "Violación de clave foránea al guardar";
                causa = "La fila referenciada no existe.";
                solucion = "Selecciona un registro padre existente antes de guardar.";
            } else if (code == 1048) {
                tipo = "Valor obligatorio nulo";
                causa = "Una columna NOT NULL recibió un valor nulo.";
                solucion = "Completa el campo obligatorio antes de guardar.";
            } else if (code == 1064 || "42000".equals(sqlState)) {
                tipo = "Error de sintaxis o consulta SQL";
                causa = "La sentencia SQL es inválida o contiene un identificador no válido.";
                solucion = "Revisa la sentencia y los nombres de tablas/columnas contra el esquema.";
            } else if ("08".equals(sqlState) || sqlState.startsWith("08")
                    || code == 2002 || code == 2003 || code == 1042) {
                tipo = "Conexión MySQL no disponible";
                causa = "MySQL está desconectado, el host/puerto es incorrecto o la conexión fue interrumpida.";
                solucion = "Inicia MySQL y valida DB_URL, el puerto 3307 y el estado del contenedor.";
            } else if ("23000".equals(sqlState)) {
                tipo = "Restricción de integridad";
                causa = "La operación viola una restricción de clave o de integridad.";
                solucion = "Revisa claves duplicadas, relaciones y restricciones de la tabla.";
            } else {
                tipo = "Error SQL";
                causa = "La base rechazó la operación o no pudo completarla.";
                solucion = "Revisa la consulta, el esquema y los permisos; conserva el mensaje original para diagnosticar.";
            }
        } else {
            tipo = original.getClass().getSimpleName();
            mensaje = original.getMessage() == null ? "Sin mensaje adicional." : original.getMessage();
            causa = "Entrada inválida o una condición de ejecución impidió completar la operación.";
            solucion = "Revisa los datos indicados y la traza de la excepción original.";
        }
        String contexto = error.getMessage() == null ? mensaje : error.getMessage();
        String origen = exceptionLocation(error, ubicacion);
        return "Tipo de error: " + tipo + " (" + (sqlError == null
                ? original.getClass().getSimpleName() : sqlError.getClass().getSimpleName()) + ")"
                + "\nMensaje específico: " + contexto
                + "\nMensaje original: " + mensaje
                + "\nClase y método: " + origen
                + "\nContexto de interfaz: " + ubicacion
                + "\nCausa probable: " + causa
                + "\nSQLState: " + sqlState
                + "\nCódigo MySQL: " + codigoMySql
                + "\nSolución recomendada: " + solucion;
    }

    private static String exceptionLocation(Throwable error, String fallback) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            for (StackTraceElement frame : current.getStackTrace()) {
                String className = frame.getClassName();
                if (className.startsWith("com.conectiontomysql.")
                        && !className.equals(Conexion.class.getName())) {
                    return className + "." + frame.getMethodName();
                }
            }
        }
        return fallback;
    }
}
