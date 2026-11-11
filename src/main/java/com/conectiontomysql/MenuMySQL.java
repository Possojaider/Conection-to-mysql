
package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MenuMySQL extends JFrame {

    public MenuMySQL() {

        setTitle("NOVA - Glaucoma Database");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        panel.setBackground(new Color(19, 23, 42));

        panel.setLayout(
            new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        JLabel titulo = new JLabel(
            "NOVA DATABASE MANAGER"
        );

        titulo.setForeground(Color.WHITE);

        titulo.setFont(
            new Font("Segoe UI", Font.BOLD, 32)
        );

        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel(
            "Sistema de administración MySQL"
        );

        subtitulo.setForeground(Color.LIGHT_GRAY);

        subtitulo.setFont(
            new Font("Segoe UI", Font.PLAIN, 16)
        );

        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // BOTONES DEL SISTEMA

        JButton btnConexion = crearBoton(
            "Probar conexión MySQL"
        );

        JButton btnBases = crearBoton(
            "Administrar bases de datos"
        );

        JButton btnTablas = crearBoton(
            "Administrar tablas"
        );

        JButton btnRegistros = crearBoton(
            "Administrar registros"
        );

        JButton btnSQL = crearBoton(
            "Ejecutar consultas SQL"
        );

        JButton btnSalir = crearBoton(
            "Salir del sistema"
        );

        // EVENTOS

        btnConexion.addActionListener(e -> probarConexion());

        btnBases.addActionListener(e ->
            new VentanaBasesDatos().setVisible(true)
        );

        btnTablas.addActionListener(e ->
            new VentanaBasesDatos().setVisible(true)
        );

        btnRegistros.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                this,
                "Primero selecciona una tabla desde Administrar bases de datos."
            );

            new VentanaBasesDatos().setVisible(true);
        });

        btnSQL.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                this,
                "El módulo de consultas SQL estará disponible cuando creemos VentanaConsultasSQL."
            );
        });

        btnSalir.addActionListener(e -> {

            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas salir de NOVA?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION
            );

            if (opcion == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // DISEÑO DEL MENU

        panel.add(Box.createVerticalGlue());

        panel.add(titulo);

        panel.add(Box.createVerticalStrut(15));

        panel.add(subtitulo);

        panel.add(Box.createVerticalStrut(40));

        panel.add(btnConexion);

        panel.add(Box.createVerticalStrut(15));

        panel.add(btnBases);

        panel.add(Box.createVerticalStrut(15));

        panel.add(btnTablas);

        panel.add(Box.createVerticalStrut(15));

        panel.add(btnRegistros);

        panel.add(Box.createVerticalStrut(15));

        panel.add(btnSQL);

        panel.add(Box.createVerticalStrut(15));

        panel.add(btnSalir);

        panel.add(Box.createVerticalGlue());

        add(panel, BorderLayout.CENTER);
    }

    // CREAR BOTONES

    private JButton crearBoton(String texto) {

        JButton boton = new JButton(texto);

        boton.setAlignmentX(Component.CENTER_ALIGNMENT);

        boton.setFont(
            new Font("Segoe UI", Font.BOLD, 15)
        );

        boton.setPreferredSize(
            new Dimension(320, 45)
        );

        boton.setMaximumSize(
            new Dimension(320, 45)
        );

        return boton;
    }

    // PROBAR CONEXION MYSQL

    private void probarConexion() {

        try (Connection conexion = Conexion.conectar()) {

            if (conexion != null && !conexion.isClosed()) {

                JOptionPane.showMessageDialog(
                    this,
                    "¡Conexión exitosa con MySQL!\n\n"
                    + "Base de datos: oftalmologia_glaucoma\n"
                    + "Servidor: 127.0.0.1:3307",
                    "Conexión exitosa",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                this,
                "Error al conectar con MySQL:\n"
                + ex.getMessage(),
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // INICIAR SISTEMA

    public static void main(String[] args) {

        System.out.println("Iniciando NOVA...");

        SwingUtilities.invokeLater(() -> {

            try {

                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
                );

                new MenuMySQL().setVisible(true);

                System.out.println(
                    "Ventana abierta correctamente."
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
        });
    }
}
