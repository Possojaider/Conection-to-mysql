
package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

public class VentanaRegistros extends JFrame {

    private JTable tabla;
    private DefaultTableModel modelo;
    private String nombreTabla;

    public VentanaRegistros(String nombreTabla) {

        this.nombreTabla = nombreTabla;

        setTitle("NOVA - Tabla: " + nombreTabla);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel titulo = new JLabel(
            "REGISTROS: " + nombreTabla
        );

        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        modelo = new DefaultTableModel();
        tabla = new JTable(modelo);

        JButton actualizar = new JButton("Actualizar registros");

        actualizar.addActionListener(e -> cargarRegistros());

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(actualizar, BorderLayout.SOUTH);

        add(panel);

        cargarRegistros();
    }

    private void cargarRegistros() {

        modelo.setRowCount(0);
        modelo.setColumnCount(0);

        // El nombre se valida contra las tablas reales de la BD.
        try (Connection con = Conexion.conectar()) {

            DatabaseMetaData metadata = con.getMetaData();

            boolean tablaExiste = false;

            try (ResultSet tablas = metadata.getTables(
                con.getCatalog(),
                null,
                nombreTabla,
                new String[]{"TABLE"}
            )) {
                tablaExiste = tablas.next();
            }

            if (!tablaExiste) {
                throw new SQLException("La tabla no existe.");
            }

            String sql = "SELECT * FROM `" +
                nombreTabla.replace("`", "``") + "`";

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                ResultSetMetaData columnas = rs.getMetaData();

                int cantidad = columnas.getColumnCount();

                for (int i = 1; i <= cantidad; i++) {
                    modelo.addColumn(
                        columnas.getColumnName(i)
                    );
                }

                while (rs.next()) {

                    Object[] fila = new Object[cantidad];

                    for (int i = 1; i <= cantidad; i++) {
                        fila[i - 1] = rs.getObject(i);
                    }

                    modelo.addRow(fila);
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Error al cargar registros:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
