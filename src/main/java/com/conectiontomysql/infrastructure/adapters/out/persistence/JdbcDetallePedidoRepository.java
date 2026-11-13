package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.ports.out.DetallePedidoRepository;

public class JdbcDetallePedidoRepository implements DetallePedidoRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(DetallePedido detallePedido) {
        String sql = "INSERT INTO detalle_pedido (codigo_pedido, codigo_producto, cantidad, precio_unidad, numero_linea) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, detallePedido.codigoPedido());
            ps.setString(2, detallePedido.codigoProducto());
            ps.setInt(3, detallePedido.cantidad());
            ps.setBigDecimal(4, detallePedido.precioUnidad());
            ps.setInt(5, detallePedido.numeroLinea());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el detalle del pedido", e);
        }
    }

    @Override
    public List<DetallePedido> findAll() {
        List<DetallePedido> detalles = new ArrayList<>();
        String sql = "SELECT * FROM detalle_pedido";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                detalles.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar los detalles", e);
        }
        return detalles;
    }

    @Override
    public Optional<DetallePedido> findById(Integer codigoPedido, String codigoProducto) {
        String sql = "SELECT * FROM detalle_pedido WHERE codigo_pedido = ? AND codigo_producto = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoPedido);
            ps.setString(2, codigoProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el detalle del pedido", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(DetallePedido detallePedido) {
        String sql = "UPDATE detalle_pedido SET cantidad = ?, precio_unidad = ?, numero_linea = ? WHERE codigo_pedido = ? AND codigo_producto = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, detallePedido.cantidad());
            ps.setBigDecimal(2, detallePedido.precioUnidad());
            ps.setInt(3, detallePedido.numeroLinea());
            ps.setInt(4, detallePedido.codigoPedido());
            ps.setString(5, detallePedido.codigoProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el detalle del pedido", e);
        }
    }

    @Override
    public boolean deleteById(Integer codigoPedido, String codigoProducto) {
        String sql = "DELETE FROM detalle_pedido WHERE codigo_pedido = ? AND codigo_producto = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoPedido);
            ps.setString(2, codigoProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el detalle del pedido", e);
        }
    }

    private DetallePedido map(ResultSet rs) throws SQLException {
        return new DetallePedido(
                rs.getInt("codigo_pedido"),
                rs.getString("codigo_producto"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("precio_unidad"),
                rs.getInt("numero_linea")
        );
    }
}
