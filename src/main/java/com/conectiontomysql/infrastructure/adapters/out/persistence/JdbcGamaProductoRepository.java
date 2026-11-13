package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.GamaProducto;
import com.conectiontomysql.domain.ports.out.GamaProductoRepository;

public class JdbcGamaProductoRepository implements GamaProductoRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(GamaProducto gamaProducto) {
        String sql = "INSERT INTO gama_producto (gama, descripcion_texto, descripcion_html, imagen) VALUES (?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gamaProducto.gama());
            ps.setString(2, gamaProducto.descripcionTexto());
            ps.setString(3, gamaProducto.descripcionHtml());
            ps.setString(4, gamaProducto.imagen());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la gama de producto", e);
        }
    }

    @Override
    public List<GamaProducto> findAll() {
        List<GamaProducto> gamas = new ArrayList<>();
        String sql = "SELECT * FROM gama_producto";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                gamas.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar las gamas", e);
        }
        return gamas;
    }

    @Override
    public Optional<GamaProducto> findById(String gama) {
        String sql = "SELECT * FROM gama_producto WHERE gama = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gama);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la gama", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(GamaProducto gamaProducto) {
        String sql = "UPDATE gama_producto SET descripcion_texto = ?, descripcion_html = ?, imagen = ? WHERE gama = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gamaProducto.descripcionTexto());
            ps.setString(2, gamaProducto.descripcionHtml());
            ps.setString(3, gamaProducto.imagen());
            ps.setString(4, gamaProducto.gama());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la gama", e);
        }
    }

    @Override
    public boolean deleteById(String gama) {
        String sql = "DELETE FROM gama_producto WHERE gama = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gama);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la gama", e);
        }
    }

    private GamaProducto map(ResultSet rs) throws SQLException {
        return new GamaProducto(
                rs.getString("gama"),
                rs.getString("descripcion_texto"),
                rs.getString("descripcion_html"),
                rs.getString("imagen")
        );
    }
}
