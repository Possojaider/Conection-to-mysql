package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Pedido;
import com.conectiontomysql.domain.ports.out.OrderTransactionPort;

public final class JdbcOrderTransactionAdapter implements OrderTransactionPort {
    private final DatabaseConfig databaseConfig;

    public JdbcOrderTransactionAdapter(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public void createAtomically(Pedido pedido, List<DetallePedido> details) {
        try (Connection connection = databaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal creditLimit = lockCustomerAndReadCredit(connection, pedido.codigoCliente());
                BigDecimal orderTotal = details.stream()
                        .map(detail -> detail.precioUnidad().multiply(BigDecimal.valueOf(detail.cantidad())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal outstandingBalance = readOutstandingBalance(connection, pedido.codigoCliente());
                if (creditLimit != null
                        && outstandingBalance.add(orderTotal).compareTo(creditLimit) > 0) {
                    throw new IllegalArgumentException(
                            "El saldo pendiente más el pedido supera el límite de crédito del cliente.");
                }

                for (DetallePedido detail : details) {
                    reserveStock(connection, detail);
                }
                insertOrder(connection, pedido);
                for (DetallePedido detail : details) {
                    insertDetail(connection, detail);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear el pedido; la operación fue cancelada.", e);
        }
    }

    private BigDecimal lockCustomerAndReadCredit(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT limite_credito FROM cliente WHERE codigo_cliente = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("El cliente seleccionado no existe.");
                }
                return result.getBigDecimal("limite_credito");
            }
        }
    }

    private BigDecimal readOutstandingBalance(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT GREATEST("
                + "COALESCE((SELECT SUM(dp.cantidad * dp.precio_unidad) "
                + "FROM pedido p JOIN detalle_pedido dp ON dp.codigo_pedido = p.codigo_pedido "
                + "WHERE p.codigo_cliente = ? AND p.estado <> 'Rechazado'), 0) "
                + "- COALESCE((SELECT SUM(total) FROM pago WHERE codigo_cliente = ?), 0), 0) AS saldo";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, customerId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBigDecimal("saldo");
            }
        }
    }

    private void reserveStock(Connection connection, DetallePedido detail) throws SQLException {
        String lockSql = "SELECT cantidad_en_stock FROM producto WHERE codigo_producto = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
            statement.setString(1, detail.codigoProducto());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("No existe el producto " + detail.codigoProducto() + ".");
                }
                int available = result.getInt("cantidad_en_stock");
                if (available < detail.cantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto "
                            + detail.codigoProducto() + ". Disponible: " + available + ".");
                }
            }
        }

        String updateSql = "UPDATE producto SET cantidad_en_stock = cantidad_en_stock - ? "
                + "WHERE codigo_producto = ? AND cantidad_en_stock >= ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setInt(1, detail.cantidad());
            statement.setString(2, detail.codigoProducto());
            statement.setInt(3, detail.cantidad());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("El stock cambió mientras se procesaba el pedido.");
            }
        }
    }

    private void insertOrder(Connection connection, Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedido (codigo_pedido, fecha_pedido, fecha_esperada, fecha_entrega, "
                + "estado, comentarios, codigo_cliente) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedido.codigoPedido());
            statement.setDate(2, java.sql.Date.valueOf(pedido.fechaPedido()));
            statement.setDate(3, java.sql.Date.valueOf(pedido.fechaEsperada()));
            if (pedido.fechaEntrega() == null) {
                statement.setNull(4, Types.DATE);
            } else {
                statement.setDate(4, java.sql.Date.valueOf(pedido.fechaEntrega()));
            }
            statement.setString(5, pedido.estado());
            statement.setString(6, pedido.comentarios());
            statement.setInt(7, pedido.codigoCliente());
            statement.executeUpdate();
        }
    }

    private void insertDetail(Connection connection, DetallePedido detail) throws SQLException {
        String sql = "INSERT INTO detalle_pedido "
                + "(codigo_pedido, codigo_producto, cantidad, precio_unidad, numero_linea) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, detail.codigoPedido());
            statement.setString(2, detail.codigoProducto());
            statement.setInt(3, detail.cantidad());
            statement.setBigDecimal(4, detail.precioUnidad());
            statement.setInt(5, detail.numeroLinea());
            statement.executeUpdate();
        }
    }

    private void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
