package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.conectiontomysql.application.ports.in.CreateOrderUseCase;
import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Pedido;

public class PedidoCreationFrame extends JFrame {
    private final JTextField orderId = new JTextField();
    private final JTextField expectedDate = new JTextField(LocalDate.now().plusDays(7).toString());
    private final JTextField comments = new JTextField();
    private final JTextField quantity = new JTextField("1");
    private final JComboBox<CustomerOption> customers = new JComboBox<>();
    private final JComboBox<ProductOption> products = new JComboBox<>();
    private final DefaultTableModel detailModel = new DefaultTableModel(
            new String[]{"Código", "Producto", "Cantidad", "Precio", "Número de línea"}, 0);
    private final List<DetallePedido> details = new ArrayList<>();

    public PedidoCreationFrame() {
        setTitle("Crear pedido");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Código del pedido"));
        form.add(orderId);
        form.add(new JLabel("Cliente"));
        form.add(customers);
        form.add(new JLabel("Fecha esperada (AAAA-MM-DD)"));
        form.add(expectedDate);
        form.add(new JLabel("Comentarios"));
        form.add(comments);
        form.add(new JLabel("Producto"));
        form.add(products);
        form.add(new JLabel("Cantidad"));
        form.add(quantity);

        JTable table = new JTable(detailModel);
        JButton addDetail = new JButton("Agregar producto");
        JButton save = new JButton("Crear pedido");
        JButton cancel = new JButton("Cancelar");
        addDetail.addActionListener(event -> addDetail());
        save.addActionListener(event -> createOrder());
        cancel.addActionListener(event -> dispose());

        JPanel actions = new JPanel();
        actions.add(addDetail);
        actions.add(save);
        actions.add(cancel);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.add(form, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
        loadOptions();
    }

    private void loadOptions() {
        try (Connection connection = Conexion.conectar();
             PreparedStatement customerQuery = connection.prepareStatement(
                     "SELECT codigo_cliente, nombre_cliente FROM cliente ORDER BY nombre_cliente");
             ResultSet customerResults = customerQuery.executeQuery()) {
            while (customerResults.next()) {
                customers.addItem(new CustomerOption(
                        customerResults.getInt("codigo_cliente"),
                        customerResults.getString("nombre_cliente")));
            }
        } catch (SQLException e) {
            showError("No se pudieron cargar los clientes", e, "PedidoCreationFrame.loadOptions");
            return;
        }
        try (Connection connection = Conexion.conectar();
             PreparedStatement productQuery = connection.prepareStatement(
                     "SELECT codigo_producto, nombre, precio_venta, cantidad_en_stock "
                             + "FROM producto ORDER BY nombre");
             ResultSet productResults = productQuery.executeQuery()) {
            while (productResults.next()) {
                products.addItem(new ProductOption(
                        productResults.getString("codigo_producto"),
                        productResults.getString("nombre"),
                        productResults.getBigDecimal("precio_venta"),
                        productResults.getInt("cantidad_en_stock")));
            }
        } catch (SQLException e) {
            showError("No se pudieron cargar los productos", e, "PedidoCreationFrame.loadOptions");
        }
    }

    private void addDetail() {
        ProductOption product = (ProductOption) products.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this, "No hay productos disponibles.");
            return;
        }
        try {
            int amount = Integer.parseInt(quantity.getText().trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
            if (amount > product.stock()) {
                throw new IllegalArgumentException("Stock disponible: " + product.stock());
            }
            int orderCode = Integer.parseInt(orderId.getText().trim());
            int lineNumber = details.size() + 1;
            DetallePedido detail = new DetallePedido(
                    orderCode, product.code(), amount, product.price(), lineNumber);
            details.add(detail);
            detailModel.addRow(new Object[]{
                    product.code(), product.name(), amount, product.price(), lineNumber});
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    Conexion.diagnosticoError(e, "PedidoCreationFrame.addDetail"),
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, Conexion.diagnosticoError(e, "PedidoCreationFrame.addDetail"),
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createOrder() {
        CustomerOption customer = (CustomerOption) customers.getSelectedItem();
        if (customer == null || details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente y agrega al menos un producto.");
            return;
        }
        try {
            int code = Integer.parseInt(orderId.getText().trim());
            LocalDate expected = LocalDate.parse(expectedDate.getText().trim());
            List<DetallePedido> orderDetails = details.stream()
                    .map(detail -> new DetallePedido(code, detail.codigoProducto(), detail.cantidad(),
                            detail.precioUnidad(), detail.numeroLinea()))
                    .toList();
            Pedido order = new Pedido(code, LocalDate.now(), expected, null,
                    "Pendiente", emptyToNull(comments.getText()), customer.code());
            CreateOrderUseCase createOrder = ApplicationFactory.createOrderUseCase();
            createOrder.create(order, orderDetails);
            JOptionPane.showMessageDialog(this, "Pedido creado correctamente; el stock fue actualizado.");
            dispose();
        } catch (NumberFormatException | java.time.DateTimeException e) {
            JOptionPane.showMessageDialog(this, Conexion.diagnosticoError(e, "PedidoCreationFrame.createOrder"),
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, Conexion.diagnosticoError(e, "PedidoCreationFrame.createOrder"),
                    "Validación de negocio",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this,
                    Conexion.diagnosticoError(e, "PedidoCreationFrame.createOrder"),
                    "No se pudo crear el pedido",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String emptyToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void showError(String message, SQLException exception, String location) {
        JOptionPane.showMessageDialog(this, message + "\n\n" + Conexion.diagnosticoError(exception, location),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    private record CustomerOption(int code, String name) {
        @Override
        public String toString() {
            return code + " — " + name;
        }
    }

    private record ProductOption(String code, String name, BigDecimal price, int stock) {
        @Override
        public String toString() {
            return code + " — " + name + " ($" + price + "; stock " + stock + ")";
        }
    }
}
