package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.application.model.EntityField;
import com.conectiontomysql.application.model.EntityOption;
import com.conectiontomysql.application.model.EntityTable;
import com.conectiontomysql.application.model.EntityValue;
import com.conectiontomysql.application.ports.out.EntityManagementPort;

public final class JdbcEntityManagementAdapter implements EntityManagementPort {
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "oficina", "empleado", "cliente", "gama_producto",
            "producto", "pedido", "detalle_pedido", "pago");
    private final DatabaseConfig databaseConfig;

    public JdbcEntityManagementAdapter(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public EntityTable load(String tableName) {
        requireTable(tableName);
        try (Connection connection = databaseConfig.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            List<EntityField> fields = new ArrayList<>();
            try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, tableName, "%")) {
                while (result.next()) {
                    ForeignKey foreignKey = findForeignKey(metadata, connection, tableName,
                            result.getString("COLUMN_NAME"));
                    fields.add(new EntityField(
                            result.getString("COLUMN_NAME"),
                            result.getInt("DATA_TYPE"),
                            result.getString("TYPE_NAME"),
                            result.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                            "YES".equalsIgnoreCase(result.getString("IS_AUTOINCREMENT")),
                            result.getInt("ORDINAL_POSITION"),
                            foreignKey == null ? null : foreignKey.table(),
                            foreignKey == null ? null : foreignKey.column()));
                }
            }
            fields.sort(Comparator.comparingInt(EntityField::ordinal));

            List<String> primaryKey = new ArrayList<>();
            try (ResultSet result = metadata.getPrimaryKeys(connection.getCatalog(), null, tableName)) {
                while (result.next()) {
                    primaryKey.add(result.getString("COLUMN_NAME"));
                }
            }

            List<Object[]> rows = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT * FROM " + quote(tableName))) {
                int columnCount = result.getMetaData().getColumnCount();
                while (result.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = result.getObject(i);
                    }
                    rows.add(row);
                }
            }
            return new EntityTable(fields, primaryKey, rows);
        } catch (SQLException e) {
            throw databaseError("No se pudo cargar " + tableName, e);
        }
    }

    @Override
    public List<EntityOption> loadOptions(String tableName, String keyColumn) {
        requireTable(tableName);
        try (Connection connection = databaseConfig.getConnection()) {
            requireColumn(connection, tableName, keyColumn);
            List<EntityOption> options = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT * FROM " + quote(tableName))) {
                ResultSetMetaData metadata = result.getMetaData();
                int labelIndex = metadata.getColumnCount() > 1 ? 2 : 1;
                while (result.next()) {
                    Object value = result.getObject(keyColumn);
                    options.add(new EntityOption(value, value + " — " + result.getString(labelIndex)));
                }
            }
            return List.copyOf(options);
        } catch (SQLException e) {
            throw databaseError("No se pudieron cargar las opciones de " + tableName, e);
        }
    }

    @Override
    public int insert(String tableName, List<EntityValue> values) {
        requireTable(tableName);
        requireValues(values);
        String columns = String.join(", ", values.stream()
                .map(value -> quote(value.name())).toList());
        String placeholders = String.join(", ", values.stream().map(value -> "?").toList());
        String sql = "INSERT INTO " + quote(tableName) + " (" + columns + ") VALUES (" + placeholders + ")";
        return execute(tableName, values, List.of(), sql);
    }

    @Override
    public int update(String tableName, List<EntityValue> values, List<EntityValue> keyValues) {
        requireTable(tableName);
        requireValues(values);
        requireValues(keyValues);
        String assignments = String.join(", ", values.stream()
                .map(value -> quote(value.name()) + " = ?").toList());
        String where = String.join(" AND ", keyValues.stream()
                .map(value -> quote(value.name()) + " = ?").toList());
        return execute(tableName, values, keyValues,
                "UPDATE " + quote(tableName) + " SET " + assignments + " WHERE " + where);
    }

    @Override
    public int delete(String tableName, List<EntityValue> keyValues) {
        requireTable(tableName);
        requireValues(keyValues);
        String where = String.join(" AND ", keyValues.stream()
                .map(value -> quote(value.name()) + " = ?").toList());
        return execute(tableName, List.of(), keyValues,
                "DELETE FROM " + quote(tableName) + " WHERE " + where);
    }

    private int execute(String tableName, List<EntityValue> values, List<EntityValue> keyValues, String sql) {
        try (Connection connection = databaseConfig.getConnection()) {
            for (EntityValue value : values) {
                requireColumn(connection, tableName, value.name());
            }
            for (EntityValue key : keyValues) {
                requireColumn(connection, tableName, key.name());
            }
            if (!keyValues.isEmpty()) {
                requirePrimaryKey(connection, tableName, keyValues);
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int parameter = bind(statement, values, 1);
                bind(statement, keyValues, parameter);
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw databaseError("No se pudo guardar " + tableName, e);
        }
    }

    private int bind(PreparedStatement statement, List<EntityValue> values, int firstIndex) throws SQLException {
        int index = firstIndex;
        for (EntityValue value : values) {
            statement.setObject(index++, value.value(), value.jdbcType());
        }
        return index;
    }

    private ForeignKey findForeignKey(DatabaseMetaData metadata, Connection connection,
            String tableName, String columnName) throws SQLException {
        try (ResultSet keys = metadata.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (keys.next()) {
                if (columnName.equals(keys.getString("FKCOLUMN_NAME"))) {
                    String referencedTable = keys.getString("PKTABLE_NAME");
                    requireTable(referencedTable);
                    return new ForeignKey(referencedTable, keys.getString("PKCOLUMN_NAME"));
                }
            }
        }
        return null;
    }

    private void requireColumn(Connection connection, String tableName, String columnName) throws SQLException {
        boolean found = false;
        try (ResultSet columns = connection.getMetaData()
                .getColumns(connection.getCatalog(), null, tableName, columnName)) {
            while (columns.next()) {
                if (columnName.equals(columns.getString("COLUMN_NAME"))) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Columna no permitida: " + columnName);
        }
    }

    private void requirePrimaryKey(Connection connection, String tableName, List<EntityValue> keyValues)
            throws SQLException {
        Set<String> actualKeys = new HashSet<>();
        try (ResultSet keys = connection.getMetaData()
                .getPrimaryKeys(connection.getCatalog(), null, tableName)) {
            while (keys.next()) {
                actualKeys.add(keys.getString("COLUMN_NAME"));
            }
        }
        Set<String> requestedKeys = new HashSet<>();
        keyValues.forEach(key -> requestedKeys.add(key.name()));
        if (actualKeys.isEmpty() || requestedKeys.size() != keyValues.size()
                || !actualKeys.equals(requestedKeys)) {
            throw new IllegalArgumentException("La operación debe identificar la clave primaria completa.");
        }
    }

    private void requireTable(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Entidad no permitida: " + tableName);
        }
    }

    private void requireValues(List<EntityValue> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un campo.");
        }
    }

    private String quote(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private IllegalStateException databaseError(String message, SQLException cause) {
        return new IllegalStateException(message + ": " + cause.getMessage(), cause);
    }

    private record ForeignKey(String table, String column) {
    }
}
