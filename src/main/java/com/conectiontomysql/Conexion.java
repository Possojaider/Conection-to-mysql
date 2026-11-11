
package com.conectiontomysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
        "jdbc:mysql://127.0.0.1:3307/oftalmologia_glaucoma";

    private static final String USER = "java_app";

    private static final String PASSWORD = "NovaMysql2026!";

    public static Connection conectar() {

        try {

            Connection conexion = DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
            );

            System.out.println("¡Conexión exitosa con MySQL!");

            return conexion;

        } catch (SQLException e) {

            System.err.println("Error de conexión:");

            System.err.println("Código: " + e.getErrorCode());

            System.err.println("Estado: " + e.getSQLState());

            System.err.println("Mensaje: " + e.getMessage());

            return null;
        }
    }
}
