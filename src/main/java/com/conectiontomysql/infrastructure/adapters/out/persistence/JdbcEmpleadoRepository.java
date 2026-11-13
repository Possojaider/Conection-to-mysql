package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.Empleado;
import com.conectiontomysql.domain.ports.out.EmpleadoRepository;

public class JdbcEmpleadoRepository implements EmpleadoRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(Empleado empleado) {
        String sql = "INSERT INTO empleado (codigo_empleado, nombre, apellido1, apellido2, extension, email, codigo_oficina, codigo_jefe, puesto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, empleado.codigoEmpleado());
            ps.setString(2, empleado.nombre());
            ps.setString(3, empleado.apellido1());
            ps.setString(4, empleado.apellido2());
            ps.setString(5, empleado.extension());
            ps.setString(6, empleado.email());
            ps.setString(7, empleado.codigoOficina());
            ps.setObject(8, empleado.codigoJefe());
            ps.setString(9, empleado.puesto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el empleado", e);
        }
    }

    @Override
    public List<Empleado> findAll() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT * FROM empleado";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar los empleados", e);
        }
        return empleados;
    }

    @Override
    public List<Empleado> findByOffice(String officeCode) {
        if (officeCode == null || officeCode.isBlank()) {
            throw new IllegalArgumentException("El código de oficina es obligatorio.");
        }
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT * FROM empleado WHERE codigo_oficina = ? ORDER BY apellido1, nombre";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, officeCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    empleados.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los empleados por oficina", e);
        }
        return empleados;
    }

    @Override
    public Optional<Empleado> findById(Integer codigoEmpleado) {
        String sql = "SELECT * FROM empleado WHERE codigo_empleado = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el empleado", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Empleado empleado) {
        String sql = "UPDATE empleado SET nombre = ?, apellido1 = ?, apellido2 = ?, extension = ?, email = ?, codigo_oficina = ?, codigo_jefe = ?, puesto = ? WHERE codigo_empleado = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, empleado.nombre());
            ps.setString(2, empleado.apellido1());
            ps.setString(3, empleado.apellido2());
            ps.setString(4, empleado.extension());
            ps.setString(5, empleado.email());
            ps.setString(6, empleado.codigoOficina());
            ps.setObject(7, empleado.codigoJefe());
            ps.setString(8, empleado.puesto());
            ps.setInt(9, empleado.codigoEmpleado());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el empleado", e);
        }
    }

    @Override
    public boolean deleteById(Integer codigoEmpleado) {
        String sql = "DELETE FROM empleado WHERE codigo_empleado = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, codigoEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el empleado", e);
        }
    }

    private Empleado map(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("codigo_empleado"),
                rs.getString("nombre"),
                rs.getString("apellido1"),
                rs.getString("apellido2"),
                rs.getString("extension"),
                rs.getString("email"),
                rs.getString("codigo_oficina"),
                (Integer) rs.getObject("codigo_jefe"),
                rs.getString("puesto")
        );
    }
}
