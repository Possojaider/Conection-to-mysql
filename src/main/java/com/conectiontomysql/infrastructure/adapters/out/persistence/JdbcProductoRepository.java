package com.conectiontomysql.infrastructure.adapters.out.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.conectiontomysql.DatabaseConfig;
import com.conectiontomysql.domain.model.Producto;
import com.conectiontomysql.domain.ports.out.ProductoRepository;

public class JdbcProductoRepository implements ProductoRepository {

    private final DatabaseConfig config = DatabaseConfig.getInstance();

    @Override
    public void save(Producto producto) {
        String sql = "INSERT INTO producto (codigo_producto, nombre, gama, dimensiones, proveedor, descripcion, cantidad_en_stock, precio_venta, precio_proveedor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, producto.codigoProducto());
            ps.setString(2, producto.nombre());
            ps.setString(3, producto.gama());
            ps.setString(4, producto.dimensiones());
            ps.setString(5, producto.proveedor());
            ps.setString(6, producto.descripcion());
            ps.setInt(7, producto.cantidadEnStock());
            ps.setBigDecimal(8, producto.precioVenta());
            ps.setBigDecimal(9, producto.precioProveedor());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el producto", e);
        }
    }

    @Override
    public List<Producto> findAll() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar los productos", e);
        }
        return productos;
    }

    @Override
    public List<Producto> findLowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("El umbral de stock no puede ser negativo.");
        }
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE cantidad_en_stock <= ? ORDER BY cantidad_en_stock, nombre";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los productos con stock bajo", e);
        }
        return productos;
    }

    @Override
    public Optional<Producto> findById(String codigoProducto) {
        String sql = "SELECT * FROM producto WHERE codigo_producto = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigoProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el producto", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, gama = ?, dimensiones = ?, proveedor = ?, descripcion = ?, cantidad_en_stock = ?, precio_venta = ?, precio_proveedor = ? WHERE codigo_producto = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, producto.nombre());
            ps.setString(2, producto.gama());
            ps.setString(3, producto.dimensiones());
            ps.setString(4, producto.proveedor());
            ps.setString(5, producto.descripcion());
            ps.setInt(6, producto.cantidadEnStock());
            ps.setBigDecimal(7, producto.precioVenta());
            ps.setBigDecimal(8, producto.precioProveedor());
            ps.setString(9, producto.codigoProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el producto", e);
        }
    }

    @Override
    public boolean deleteById(String codigoProducto) {
        String sql = "DELETE FROM producto WHERE codigo_producto = ?";
        try (Connection connection = config.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigoProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el producto", e);
        }
    }

    private Producto map(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getString("codigo_producto"),
                rs.getString("nombre"),
                rs.getString("gama"),
                rs.getString("dimensiones"),
                rs.getString("proveedor"),
                rs.getString("descripcion"),
                rs.getInt("cantidad_en_stock"),
                rs.getBigDecimal("precio_venta"),
                rs.getBigDecimal("precio_proveedor")
        );
    }
}
