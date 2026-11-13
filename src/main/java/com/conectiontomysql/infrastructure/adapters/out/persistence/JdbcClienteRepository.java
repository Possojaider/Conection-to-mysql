package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.Cliente;
import com.conectiontomysql.domain.ports.out.ClienteRepository;

public class JdbcClienteRepository implements ClienteRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(Cliente cliente) {
        String sql = "INSERT INTO cliente (codigo_cliente, nombre_cliente, nombre_contacto, apellido_contacto, telefono, fax, linea_direccion1, linea_direccion2, ciudad, region, pais, codigo_postal, codigo_empleado_rep_ventas, limite_credito, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cliente.codigoCliente());
            ps.setString(2, cliente.nombreCliente());
            ps.setString(3, cliente.nombreContacto());
            ps.setString(4, cliente.apellidoContacto());
            ps.setString(5, cliente.telefono());
            ps.setString(6, cliente.fax());
            ps.setString(7, cliente.lineaDireccion1());
            ps.setString(8, cliente.lineaDireccion2());
            ps.setString(9, cliente.ciudad());
            ps.setString(10, cliente.region());
            ps.setString(11, cliente.pais());
            ps.setString(12, cliente.codigoPostal());
            ps.setObject(13, cliente.codigoEmpleadoRepVentas());
            ps.setBigDecimal(14, cliente.limiteCredito());
            ps.setBoolean(15, cliente.activo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el cliente", e);
        }
    }

    @Override
    public List<Cliente> findAll() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar los clientes", e);
        }
        return clientes;
    }

    @Override
    public Optional<Cliente> findById(Integer codigoCliente) {
        String sql = "SELECT * FROM cliente WHERE codigo_cliente = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el cliente", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre_cliente = ?, nombre_contacto = ?, apellido_contacto = ?, telefono = ?, fax = ?, linea_direccion1 = ?, linea_direccion2 = ?, ciudad = ?, region = ?, pais = ?, codigo_postal = ?, codigo_empleado_rep_ventas = ?, limite_credito = ?, activo = ? WHERE codigo_cliente = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cliente.nombreCliente());
            ps.setString(2, cliente.nombreContacto());
            ps.setString(3, cliente.apellidoContacto());
            ps.setString(4, cliente.telefono());
            ps.setString(5, cliente.fax());
            ps.setString(6, cliente.lineaDireccion1());
            ps.setString(7, cliente.lineaDireccion2());
            ps.setString(8, cliente.ciudad());
            ps.setString(9, cliente.region());
            ps.setString(10, cliente.pais());
            ps.setString(11, cliente.codigoPostal());
            ps.setObject(12, cliente.codigoEmpleadoRepVentas());
            ps.setBigDecimal(13, cliente.limiteCredito());
            ps.setBoolean(14, cliente.activo());
            ps.setInt(15, cliente.codigoCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el cliente", e);
        }
    }

    @Override
    public boolean deleteById(Integer codigoCliente) {
        String sql = "DELETE FROM cliente WHERE codigo_cliente = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el cliente", e);
        }
    }

    private Cliente map(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("codigo_cliente"),
                rs.getString("nombre_cliente"),
                rs.getString("nombre_contacto"),
                rs.getString("apellido_contacto"),
                rs.getString("telefono"),
                rs.getString("fax"),
                rs.getString("linea_direccion1"),
                rs.getString("linea_direccion2"),
                rs.getString("ciudad"),
                rs.getString("region"),
                rs.getString("pais"),
                rs.getString("codigo_postal"),
                (Integer) rs.getObject("codigo_empleado_rep_ventas"),
                rs.getBigDecimal("limite_credito"),
                rs.getBoolean("activo")
        );
    }
}
