package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.Pago;
import com.conectiontomysql.domain.ports.out.PagoRepository;

public class JdbcPagoRepository implements PagoRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(Pago pago) {
        String sql = "INSERT INTO pago (codigo_cliente, forma_pago, id_transaccion, fecha_pago, total) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pago.codigoCliente());
            ps.setString(2, pago.formaPago());
            ps.setString(3, pago.idTransaccion());
            ps.setDate(4, Date.valueOf(pago.fechaPago()));
            ps.setBigDecimal(5, pago.total());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el pago", e);
        }
    }

    @Override
    public List<Pago> findAll() {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM pago";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pagos.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar los pagos", e);
        }
        return pagos;
    }

    @Override
    public List<Pago> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("El rango de fechas no es válido.");
        }
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM pago WHERE fecha_pago BETWEEN ? AND ? ORDER BY fecha_pago";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los pagos por rango de fechas", e);
        }
        return pagos;
    }

    @Override
    public Optional<Pago> findById(Integer codigoCliente, String idTransaccion) {
        String sql = "SELECT * FROM pago WHERE codigo_cliente = ? AND id_transaccion = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoCliente);
            ps.setString(2, idTransaccion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el pago", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Pago pago) {
        String sql = "UPDATE pago SET forma_pago = ?, fecha_pago = ?, total = ? WHERE codigo_cliente = ? AND id_transaccion = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pago.formaPago());
            ps.setDate(2, Date.valueOf(pago.fechaPago()));
            ps.setBigDecimal(3, pago.total());
            ps.setInt(4, pago.codigoCliente());
            ps.setString(5, pago.idTransaccion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el pago", e);
        }
    }

    @Override
    public boolean deleteById(Integer codigoCliente, String idTransaccion) {
        String sql = "DELETE FROM pago WHERE codigo_cliente = ? AND id_transaccion = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoCliente);
            ps.setString(2, idTransaccion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el pago", e);
        }
    }

    private Pago map(ResultSet rs) throws SQLException {
        return new Pago(
                rs.getInt("codigo_cliente"),
                rs.getString("forma_pago"),
                rs.getString("id_transaccion"),
                rs.getDate("fecha_pago").toLocalDate(),
                rs.getBigDecimal("total")
        );
    }
}
