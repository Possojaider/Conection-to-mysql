
package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class VentanaBasesDatos extends JFrame {

    private JTable tabla;
    private DefaultTableModel modelo;

    public VentanaBasesDatos() {

        setTitle("NOVA - Explorador de tablas");
        setSize(800, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel(
            "TABLAS DE OFTALMOLOGIA_GLAUCOMA"
        );

        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        modelo = new DefaultTableModel(
            new String[]{"Nombre de tabla"}, 0
        );

        tabla = new JTable(modelo);

        JButton actualizar = new JButton("Actualizar tablas");
        JButton abrir = new JButton("Abrir tabla");

        JPanel botones = new JPanel();
        botones.add(actualizar);
        botones.add(abrir);

        actualizar.addActionListener(e -> cargarTablas());
        abrir.addActionListener(e -> abrirTabla());

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirTabla();
                }
            }
        });

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        add(panel);

        cargarTablas();
    }

    private void cargarTablas() {

        modelo.setRowCount(0);

        String sql = "SHOW FULL TABLES";

        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                modelo.addRow(new Object[]{rs.getString(1)});
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Error al consultar tablas:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void abrirTabla() {

        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Selecciona una tabla."
            );
            return;
        }

        String nombre = modelo.getValueAt(fila, 0).toString();

        new VentanaRegistros(nombre).setVisible(true);
    }
}
