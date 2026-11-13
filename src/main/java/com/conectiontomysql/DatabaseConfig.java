package com.conectiontomysql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final class Holder {
        private static final DatabaseConfig INSTANCE = new DatabaseConfig();
    }

    private final String url;
    private final String user;
    private final String password;
    private final Exception propertiesError;

    private DatabaseConfig() {
        Properties properties = new Properties();
        Exception readError = null;

        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException | IllegalArgumentException e) {
            readError = e;
        }

        this.propertiesError = readError;
        this.url = setting("DB_URL", properties, "db.url", "jdbc:mysql://localhost:3307/jardineria");
        this.user = setting("DB_USER", properties, "db.user", "java_app");
        this.password = setting("DB_PASSWORD", properties, "db.password", "");
    }

    private String setting(String environmentKey, Properties properties, String propertyKey, String fallback) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return properties.getProperty(propertyKey, fallback);
    }

    public static DatabaseConfig getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        if (propertiesError != null) {
            throw new SQLException(
                    "No se pudo leer db.properties; revisa permisos y formato antes de conectar.",
                    "HY000", 0, propertiesError);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

}
