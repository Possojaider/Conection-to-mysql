package com.conectiontomysql;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

class ConexionTest {
    @Test
    void reportsAuthenticationDetailsAndPreservesOriginalSqlException() {
        SQLException sqlException = new SQLException("Access denied", "28000", 1045);
        IllegalStateException wrapped = new IllegalStateException("No se pudo guardar", sqlException);

        String diagnostic = Conexion.diagnosticoError(wrapped, "Interfaz.guardar");

        assertTrue(diagnostic.contains("Credenciales o permisos MySQL"));
        assertTrue(diagnostic.contains("SQLState: 28000"));
        assertTrue(diagnostic.contains("Código MySQL: 1045"));
        assertTrue(diagnostic.contains("ConexionTest.reportsAuthenticationDetailsAndPreservesOriginalSqlException"));
        assertTrue(diagnostic.contains("Solución recomendada:"));
        assertSame(sqlException, wrapped.getCause());
    }

    @Test
    void classifiesMissingTablesAndRetainsTheOriginalMessage() {
        SQLException sqlException = new SQLException("Table 'jardineria.pedido' doesn't exist", "42S02", 1146);

        String diagnostic = Conexion.diagnosticoError(sqlException, "VentanaDashboard.cargar");

        assertTrue(diagnostic.contains("Tabla inexistente"));
        assertTrue(diagnostic.contains("Mensaje original: Table 'jardineria.pedido' doesn't exist"));
        assertTrue(diagnostic.contains("SQLState: 42S02"));
        assertTrue(diagnostic.contains("Código MySQL: 1146"));
        assertTrue(diagnostic.contains("Clase y método: com.conectiontomysql.ConexionTest."));
    }
}
