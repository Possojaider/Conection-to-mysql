package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.Oficina;
import com.conectiontomysql.domain.ports.out.OficinaRepository;

public class JdbcOficinaRepository implements OficinaRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(Oficina oficina) {
        String sql = "INSERT INTO oficina (codigo_oficina, ciudad, pais, region, codigo_postal, telefono, linea_direccion1, linea_direccion2) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, oficina.codigoOficina());
            ps.setString(2, oficina.ciudad());
            ps.setString(3, oficina.pais());
            ps.setString(4, oficina.region());
            ps.setString(5, oficina.codigoPostal());
            ps.setString(6, oficina.telefono());
            ps.setString(7, oficina.lineaDireccion1());
            ps.setString(8, oficina.lineaDireccion2());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la oficina", e);
        }
    }

    @Override
    public List<Oficina> findAll() {
        List<Oficina> oficinas = new ArrayList<>();
        String sql = "SELECT * FROM oficina";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                oficinas.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar las oficinas", e);
        }
        return oficinas;
    }

    @Override
    public Optional<Oficina> findById(String codigoOficina) {
        String sql = "SELECT * FROM oficina WHERE codigo_oficina = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigoOficina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la oficina", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Oficina oficina) {
        String sql = "UPDATE oficina SET ciudad = ?, pais = ?, region = ?, codigo_postal = ?, telefono = ?, linea_direccion1 = ?, linea_direccion2 = ? WHERE codigo_oficina = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, oficina.ciudad());
            ps.setString(2, oficina.pais());
            ps.setString(3, oficina.region());
            ps.setString(4, oficina.codigoPostal());
            ps.setString(5, oficina.telefono());
            ps.setString(6, oficina.lineaDireccion1());
            ps.setString(7, oficina.lineaDireccion2());
            ps.setString(8, oficina.codigoOficina());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la oficina", e);
        }
    }

    @Override
    public boolean deleteById(String codigoOficina) {
        String sql = "DELETE FROM oficina WHERE codigo_oficina = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigoOficina);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la oficina", e);
        }
    }

    private Oficina map(ResultSet rs) throws SQLException {
        return new Oficina(
                rs.getString("codigo_oficina"),
                rs.getString("ciudad"),
                rs.getString("pais"),
                rs.getString("region"),
                rs.getString("codigo_postal"),
                rs.getString("telefono"),
                rs.getString("linea_direccion1"),
                rs.getString("linea_direccion2")
        );
    }
}
