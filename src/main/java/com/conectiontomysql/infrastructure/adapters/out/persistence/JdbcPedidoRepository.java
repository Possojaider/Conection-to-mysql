package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.Pedido;
import com.conectiontomysql.domain.ports.out.PedidoRepository;

public class JdbcPedidoRepository implements PedidoRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(Pedido pedido) {
        String sql = "INSERT INTO pedido (codigo_pedido, fecha_pedido, fecha_esperada, fecha_entrega, estado, comentarios, codigo_cliente) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pedido.codigoPedido());
            ps.setDate(2, Date.valueOf(pedido.fechaPedido()));
            ps.setDate(3, Date.valueOf(pedido.fechaEsperada()));
            if (pedido.fechaEntrega() == null) {
                ps.setNull(4, java.sql.Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(pedido.fechaEntrega()));
            }
            ps.setString(5, pedido.estado());
            ps.setString(6, pedido.comentarios());
            ps.setInt(7, pedido.codigoCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el pedido", e);
        }
    }

    @Override
    public List<Pedido> findAll() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pedidos.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar los pedidos", e);
        }
        return pedidos;
    }

    @Override
    public List<Pedido> findByStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio.");
        }
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido WHERE estado = ? ORDER BY fecha_pedido DESC";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los pedidos por estado", e);
        }
        return pedidos;
    }

    @Override
    public Optional<Pedido> findById(Integer codigoPedido) {
        String sql = "SELECT * FROM pedido WHERE codigo_pedido = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el pedido", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Pedido pedido) {
        String sql = "UPDATE pedido SET fecha_pedido = ?, fecha_esperada = ?, fecha_entrega = ?, estado = ?, comentarios = ?, codigo_cliente = ? WHERE codigo_pedido = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(pedido.fechaPedido()));
            ps.setDate(2, Date.valueOf(pedido.fechaEsperada()));
            if (pedido.fechaEntrega() == null) {
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                ps.setDate(3, Date.valueOf(pedido.fechaEntrega()));
            }
            ps.setString(4, pedido.estado());
            ps.setString(5, pedido.comentarios());
            ps.setInt(6, pedido.codigoCliente());
            ps.setInt(7, pedido.codigoPedido());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el pedido", e);
        }
    }

    @Override
    public boolean deleteById(Integer codigoPedido) {
        String sql = "DELETE FROM pedido WHERE codigo_pedido = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el pedido", e);
        }
    }

    private Pedido map(ResultSet rs) throws SQLException {
        return new Pedido(
                rs.getInt("codigo_pedido"),
                rs.getDate("fecha_pedido").toLocalDate(),
                rs.getDate("fecha_esperada").toLocalDate(),
                rs.getDate("fecha_entrega") == null ? null : rs.getDate("fecha_entrega").toLocalDate(),
                rs.getString("estado"),
                rs.getString("comentarios"),
                rs.getInt("codigo_cliente")
        );
    }
}
