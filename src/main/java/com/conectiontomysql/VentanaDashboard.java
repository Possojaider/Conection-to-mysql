package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class VentanaDashboard extends JFrame {
    private static final DateTimeFormatter REFRESH_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final JLabel clientesValue = metricValue(new Color(39, 118, 255));
    private final JLabel productosValue = metricValue(new Color(34, 197, 94));
    private final JLabel pendientesValue = metricValue(new Color(245, 158, 11));
    private final JLabel entregadosValue = metricValue(new Color(16, 150, 105));
    private final JLabel ingresosValue = metricValue(new Color(168, 85, 247));
    private final JLabel stockCriticoValue = metricValue(new Color(220, 38, 38));
    private final DefaultTableModel stockModel = readonlyModel("Producto", "Stock", "Precio");
    private final DefaultTableModel ordersModel = readonlyModel("Pedido", "Fecha", "Cliente", "Estado", "Total");
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JButton refresh = new JButton("Actualizar");
    private final JLabel refreshStatus = new JLabel(" ");
    private final Timer refreshTimer = new Timer(60_000, event -> loadDashboard());
    private SwingWorker<DashboardData, Void> dashboardWorker;

    public VentanaDashboard() {
        setTitle("Jardinería - Dashboard");
        setSize(1360, 900);
        setMinimumSize(new Dimension(1050, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(new Color(244, 247, 250));

        JLabel title = new JLabel("Dashboard operativo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(24, 40, 72));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        refreshStatus.setForeground(new Color(94, 108, 124));
        refreshStatus.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        header.add(refreshStatus, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(1000, 240));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        cards.add(card("Clientes activos", clientesValue));
        cards.add(card("Productos en catálogo", productosValue));
        cards.add(card("Pedidos pendientes", pendientesValue));
        cards.add(card("Pedidos entregados", entregadosValue));
        cards.add(card("Pagos registrados (histórico)", ingresosValue));
        cards.add(card("Stock crítico (≤ 5)", stockCriticoValue));

        JTable stockTable = new JTable(stockModel);
        styleTable(stockTable);
        JTable ordersTable = new JTable(ordersModel);
        styleTable(ordersTable);
        JPanel analytics = new JPanel(new GridLayout(1, 2, 14, 14));
        analytics.setOpaque(false);
        analytics.setPreferredSize(new Dimension(1000, 340));
        analytics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        analytics.add(createPanel("Importe de pedidos entregados por gama", chartContainer));
        analytics.add(createPanel("Todos los productos con stock crítico", new JScrollPane(stockTable)));

        JPanel recentOrders = createPanel("Últimos pedidos", new JScrollPane(ordersTable));
        recentOrders.setPreferredSize(new Dimension(1000, 290));
        recentOrders.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(cards);
        content.add(Box.createVerticalStrut(16));
        content.add(analytics);
        content.add(Box.createVerticalStrut(16));
        content.add(recentOrders);

        JScrollPane dashboardScroll = new JScrollPane(content);
        dashboardScroll.setBorder(null);
        dashboardScroll.setOpaque(false);
        dashboardScroll.getViewport().setOpaque(false);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(18);
        root.add(dashboardScroll, BorderLayout.CENTER);

        setContentPane(root);
        refresh.addActionListener(event -> loadDashboard());
        refreshTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                refreshTimer.stop();
            }
        });
        loadDashboard();
    }

    private static JLabel metricValue(Color color) {
        JLabel value = new JLabel("—");
        value.setFont(new Font("Segoe UI", Font.BOLD, 28));
        value.setForeground(color);
        value.setHorizontalAlignment(SwingConstants.LEFT);
        return value;
    }

    private JPanel card(String titleText, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 231, 239)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel title = new JLabel(titleText);
        title.setForeground(new Color(94, 108, 124));
        title.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPanel accent = new JPanel();
        accent.setBackground(value.getForeground());
        accent.setPreferredSize(new Dimension(6, 0));
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(title);
        labels.add(new JLabel(" "));
        labels.add(value);
        card.add(accent, BorderLayout.WEST);
        card.add(labels, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPanel(String titleText, java.awt.Component content) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 220, 232)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(25, 44, 72));
        panel.add(title, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private static DefaultTableModel readonlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(24, 40, 72));
    }

    private void loadDashboard() {
        if (dashboardWorker != null) {
            return;
        }
        refresh.setEnabled(false);
        refreshStatus.setForeground(new Color(94, 108, 124));
        refreshStatus.setText("Actualizando datos…");
        refreshStatus.setToolTipText(null);
        dashboardWorker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                try (Connection connection = Conexion.conectar()) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                    connection.setReadOnly(true);
                    connection.setAutoCommit(false);
                    try {
                        int customers = count(connection, "SELECT COUNT(*) FROM cliente WHERE activo = 1");
                        int products = count(connection, "SELECT COUNT(*) FROM producto");
                        int pending = count(connection, "SELECT COUNT(*) FROM pedido WHERE estado = 'Pendiente'");
                        int delivered = count(connection, "SELECT COUNT(*) FROM pedido WHERE estado = 'Entregado'");
                        int criticalStock = count(connection,
                                "SELECT COUNT(*) FROM producto WHERE cantidad_en_stock <= 5");
                        BigDecimal income = sum(connection, "SELECT COALESCE(SUM(total), 0) FROM pago");

                        DefaultCategoryDataset salesByRange = new DefaultCategoryDataset();
                        String chartSql = "SELECT gp.gama, COALESCE(SUM(ventas_producto.ventas), 0) AS ventas "
                                + "FROM gama_producto gp LEFT JOIN producto p ON p.gama = gp.gama "
                                + "LEFT JOIN (SELECT dp.codigo_producto, "
                                + "SUM(dp.cantidad * dp.precio_unidad) AS ventas "
                                + "FROM detalle_pedido dp JOIN pedido pe ON pe.codigo_pedido = dp.codigo_pedido "
                                + "WHERE pe.estado = 'Entregado' GROUP BY dp.codigo_producto) ventas_producto "
                                + "ON ventas_producto.codigo_producto = p.codigo_producto "
                                + "GROUP BY gp.gama ORDER BY ventas DESC";
                        try (PreparedStatement statement = connection.prepareStatement(chartSql);
                             ResultSet results = statement.executeQuery()) {
                            while (results.next()) {
                                salesByRange.addValue(results.getBigDecimal("ventas"), "Entregado",
                                        results.getString("gama"));
                            }
                        }

                        DefaultTableModel stock = readonlyModel("Producto", "Stock", "Precio");
                        try (PreparedStatement statement = connection.prepareStatement(
                                "SELECT nombre, cantidad_en_stock, precio_venta FROM producto "
                                        + "WHERE cantidad_en_stock <= 5 ORDER BY cantidad_en_stock, nombre");
                             ResultSet results = statement.executeQuery()) {
                            while (results.next()) {
                                stock.addRow(new Object[]{
                                        results.getString(1), results.getInt(2), results.getBigDecimal(3)});
                            }
                        }

                        DefaultTableModel orders = readonlyModel("Pedido", "Fecha", "Cliente", "Estado", "Total");
                        String ordersSql = "SELECT p.codigo_pedido, p.fecha_pedido, c.nombre_cliente, p.estado, "
                                + "COALESCE(SUM(dp.cantidad * dp.precio_unidad), 0) AS total "
                                + "FROM pedido p JOIN cliente c ON c.codigo_cliente = p.codigo_cliente "
                                + "LEFT JOIN detalle_pedido dp ON dp.codigo_pedido = p.codigo_pedido "
                                + "GROUP BY p.codigo_pedido, p.fecha_pedido, c.nombre_cliente, p.estado "
                                + "ORDER BY p.fecha_pedido DESC, p.codigo_pedido DESC LIMIT 10";
                        try (PreparedStatement statement = connection.prepareStatement(ordersSql);
                             ResultSet results = statement.executeQuery()) {
                            while (results.next()) {
                                orders.addRow(new Object[]{results.getInt(1), results.getDate(2),
                                        results.getString(3), results.getString(4), results.getBigDecimal(5)});
                            }
                        }
                        connection.commit();
                        return new DashboardData(customers, products, pending, delivered, criticalStock,
                                income, salesByRange, stock, orders, LocalDateTime.now());
                    } catch (SQLException | RuntimeException e) {
                        try {
                            connection.rollback();
                        } catch (SQLException rollbackError) {
                            e.addSuppressed(rollbackError);
                        }
                        throw e;
                    }
                }
            }

            @Override
            protected void done() {
                refresh.setEnabled(true);
                try {
                    DashboardData data = get();
                    clientesValue.setText(Integer.toString(data.customers()));
                    productosValue.setText(Integer.toString(data.products()));
                    pendientesValue.setText(Integer.toString(data.pending()));
                    entregadosValue.setText(Integer.toString(data.delivered()));
                    ingresosValue.setText(data.income().toPlainString());
                    stockCriticoValue.setText(Integer.toString(data.criticalStock()));
                    replaceRows(stockModel, data.stock());
                    replaceRows(ordersModel, data.latestOrders());

                    JFreeChart chart = ChartFactory.createBarChart(null, "Gama", "Importe entregado",
                            data.salesByRange(), PlotOrientation.VERTICAL, false, true, false);
                    chart.setBackgroundPaint(Color.WHITE);
                    chartContainer.removeAll();
                    chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
                    chartContainer.revalidate();
                    chartContainer.repaint();
                    refreshStatus.setForeground(new Color(16, 120, 78));
                    refreshStatus.setText("Datos actualizados: "
                            + data.refreshedAt().format(REFRESH_TIME_FORMAT));
                    refreshStatus.setToolTipText(null);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showRefreshError("Se interrumpió la carga del dashboard.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    showRefreshError(Conexion.diagnosticoError(
                            cause, "VentanaDashboard.refreshDashboard"));
                } finally {
                    dashboardWorker = null;
                }
            }
        };
        dashboardWorker.execute();
    }

    private void showRefreshError(String message) {
        refreshStatus.setForeground(new Color(190, 30, 30));
        refreshStatus.setText(message);
        refreshStatus.setToolTipText(message);
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    private BigDecimal sum(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getBigDecimal(1);
        }
    }

    private void replaceRows(DefaultTableModel target, DefaultTableModel source) {
        target.setRowCount(0);
        for (int row = 0; row < source.getRowCount(); row++) {
            Object[] values = new Object[source.getColumnCount()];
            for (int column = 0; column < values.length; column++) {
                values[column] = source.getValueAt(row, column);
            }
            target.addRow(values);
        }
    }

    private record DashboardData(int customers, int products, int pending, int delivered, int criticalStock,
            BigDecimal income, DefaultCategoryDataset salesByRange, DefaultTableModel stock,
            DefaultTableModel latestOrders, LocalDateTime refreshedAt) {
    }
}
