package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class VentanaModulos extends JFrame {
    private static final List<Entity> COMMERCIAL = List.of(
            new Entity("Clientes", "cliente"),
            new Entity("Pedidos", "pedido"),
            new Entity("Detalles de pedidos", "detalle_pedido"),
            new Entity("Pagos", "pago"));
    private static final List<Entity> CATALOG = List.of(
            new Entity("Productos", "producto"),
            new Entity("Gamas de producto", "gama_producto"));
    private static final List<Entity> ORGANIZATION = List.of(
            new Entity("Oficinas", "oficina"),
            new Entity("Empleados", "empleado"));

    public VentanaModulos() {
        this(0);
    }

    public VentanaModulos(int selectedCategory) {
        setTitle("Jardinería - Módulos");
        setSize(850, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Módulos de gestión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        root.add(title, BorderLayout.NORTH);

        JTabbedPane categories = new JTabbedPane();
        categories.addTab("Gestión Comercial", createCategory(COMMERCIAL, true));
        categories.addTab("Catálogo e Inventario", createCategory(CATALOG, false));
        categories.addTab("Estructura Organizacional", createCategory(ORGANIZATION, false));
        categories.setSelectedIndex(selectedCategory);
        root.add(categories, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createCategory(List<Entity> entities, boolean includeOrderCreator) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 12, 12, 12));
        panel.add(new JLabel("Selecciona un módulo para consultar, buscar y administrar sus registros."),
                BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(0, 2, 12, 12));
        for (Entity entity : entities) {
            JButton button = new JButton(entity.label());
            button.setPreferredSize(new Dimension(260, 56));
            button.addActionListener(event -> new EntityCrudFrame(entity.table()).setVisible(true));
            buttons.add(button);
        }
        if (includeOrderCreator) {
            JButton createOrder = new JButton("Crear pedido con detalles");
            createOrder.setPreferredSize(new Dimension(260, 56));
            createOrder.addActionListener(event -> new PedidoCreationFrame().setVisible(true));
            buttons.add(createOrder);
        }
        panel.add(buttons, BorderLayout.CENTER);
        return panel;
    }

    private record Entity(String label, String table) {
    }
}
