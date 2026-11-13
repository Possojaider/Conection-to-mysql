package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;

public class MenuMySQL extends JFrame {
        private static final Color BACKGROUND = new Color(15, 23, 31);
        private static final Color SURFACE = new Color(25, 36, 43);
        private static final Color TEXT = new Color(239, 245, 241);
        private static final Color MUTED = new Color(160, 177, 171);
        private static final Color BORDER = new Color(48, 65, 68);

        public MenuMySQL() {
                setTitle("Jardinería | Centro de gestión");
                setSize(1120, 800);
                setMinimumSize(new Dimension(900, 680));
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel root = new JPanel(new BorderLayout(0, 0));
                root.setBackground(BACKGROUND);

                root.add(createHeader(), BorderLayout.NORTH);
                root.add(createMainContent(), BorderLayout.CENTER);
                root.add(createFooter(), BorderLayout.SOUTH);
                setContentPane(root);
        }

        private JPanel createHeader() {
                JPanel header = new JPanel(new BorderLayout(20, 12));
                header.setBackground(new Color(20, 54, 43));
                header.setBorder(BorderFactory.createEmptyBorder(30, 42, 30, 42));

                JPanel brand = new JPanel();
                brand.setOpaque(false);
                brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

                JLabel eyebrow = new JLabel("JARDINERÍA  /  OPERACIONES");
                eyebrow.setForeground(new Color(148, 218, 183));
                eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JLabel title = new JLabel("Centro de gestión");
                title.setForeground(TEXT);
                title.setFont(new Font("Segoe UI", Font.BOLD, 32));
                title.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

                JLabel subtitle = new JLabel(
                                "Administra clientes, pedidos, productos y operaciones desde un solo lugar.");
                subtitle.setForeground(new Color(196, 216, 205));
                subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                brand.add(eyebrow);
                brand.add(title);
                brand.add(subtitle);
                header.add(brand, BorderLayout.CENTER);

                JLabel badge = new JLabel("  MYSQL  ·  8 MÓDULOS  ");
                badge.setForeground(new Color(205, 241, 220));
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setOpaque(true);
                badge.setBackground(new Color(32, 82, 61));
                badge.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(68, 129, 94)),
                                BorderFactory.createEmptyBorder(9, 4, 9, 4)));
                badge.setHorizontalAlignment(JLabel.CENTER);
                header.add(badge, BorderLayout.EAST);
                return header;
        }

        private JPanel createMainContent() {
                JPanel content = new JPanel(new BorderLayout(0, 16));
                content.setBackground(BACKGROUND);
                content.setBorder(BorderFactory.createEmptyBorder(26, 42, 24, 42));

                JPanel sectionHeader = new JPanel(new BorderLayout());
                sectionHeader.setOpaque(false);
                JLabel sectionTitle = new JLabel("Accesos directos");
                sectionTitle.setForeground(TEXT);
                sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
                JLabel sectionHint = new JLabel("Selecciona un módulo para comenzar");
                sectionHint.setForeground(MUTED);
                sectionHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                sectionHeader.add(sectionTitle, BorderLayout.WEST);
                sectionHeader.add(sectionHint, BorderLayout.EAST);
                content.add(sectionHeader, BorderLayout.NORTH);

                JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
                cards.setOpaque(false);
                cards.add(createCard("01", "Dashboard", "Indicadores, pedidos recientes y alertas de inventario.",
                                new Color(66, 180, 129), () -> new VentanaDashboard().setVisible(true)));
                cards.add(createCard("02", "Gestión comercial", "Administra clientes, pedidos, detalles y pagos.",
                                new Color(79, 159, 190), () -> new VentanaModulos(0).setVisible(true)));
                cards.add(createCard("03", "Catálogo e inventario", "Gestiona productos, gamas y existencias.",
                                new Color(145, 174, 89), () -> new VentanaModulos(1).setVisible(true)));
                cards.add(createCard("04", "Estructura organizacional", "Consulta oficinas y administra empleados.",
                                new Color(170, 139, 202), () -> new VentanaModulos(2).setVisible(true)));
                cards.add(createCard("05", "Explorar base de datos", "Accede a las tablas y registros de Jardinería.",
                                new Color(205, 157, 91), () -> new VentanaBasesDatos().setVisible(true)));
                cards.add(createCard("06", "Conexión MySQL",
                                "Comprueba que la aplicación pueda acceder a la base de datos.",
                                new Color(82, 177, 164), this::probarConexion));

                JScrollPane scroll = new JScrollPane(cards);
                scroll.setBorder(null);
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                content.add(scroll, BorderLayout.CENTER);
                return content;
        }

        private JPanel createCard(String number, String title, String description, Color accent, Runnable action) {
                JPanel card = new JPanel(new BorderLayout(12, 12));
                card.setBackground(SURFACE);
                card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER),
                                BorderFactory.createEmptyBorder(18, 18, 16, 18)));
                card.setMinimumSize(new Dimension(220, 190));

                JPanel cardHeader = new JPanel(new BorderLayout());
                cardHeader.setOpaque(false);
                JLabel numberLabel = new JLabel(number);
                numberLabel.setForeground(accent);
                numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                JLabel dot = new JLabel("●");
                dot.setForeground(accent);
                dot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cardHeader.add(numberLabel, BorderLayout.WEST);
                cardHeader.add(dot, BorderLayout.EAST);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setForeground(TEXT);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));

                JLabel descriptionLabel = new JLabel(
                                "<html><div style='width:220px'>" + description + "</div></html>");
                descriptionLabel.setForeground(MUTED);
                descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                descriptionLabel.setVerticalAlignment(JLabel.TOP);

                JPanel copy = new JPanel(new BorderLayout(0, 8));
                copy.setOpaque(false);
                copy.add(titleLabel, BorderLayout.NORTH);
                copy.add(descriptionLabel, BorderLayout.CENTER);

                JButton openButton = new JButton("Abrir módulo  →");
                openButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
                openButton.setForeground(accent);
                openButton.setBackground(SURFACE);
                openButton.setFocusPainted(false);
                openButton.setBorderPainted(false);
                openButton.setHorizontalAlignment(JButton.LEFT);
                openButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                openButton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                openButton.addActionListener(event -> action.run());

                card.add(cardHeader, BorderLayout.NORTH);
                card.add(copy, BorderLayout.CENTER);
                card.add(openButton, BorderLayout.SOUTH);
                return card;
        }

        private JPanel createFooter() {
                JPanel footer = new JPanel(new BorderLayout());
                footer.setBackground(new Color(19, 29, 35));
                footer.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                                BorderFactory.createEmptyBorder(12, 42, 12, 42)));

                JLabel note = new JLabel("Jardinería  ·  Sistema de administración");
                note.setForeground(MUTED);
                note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JButton exit = new JButton("Salir");
                exit.setForeground(new Color(220, 155, 155));
                exit.setFont(new Font("Segoe UI", Font.BOLD, 12));
                exit.putClientProperty("JButton.buttonType", "borderless");
                exit.addActionListener(event -> confirmExit());

                footer.add(note, BorderLayout.WEST);
                footer.add(exit, BorderLayout.EAST);
                return footer;
        }

        private void confirmExit() {
                int option = JOptionPane.showConfirmDialog(
                                this,
                                "¿Deseas salir de JARDINERÍA?",
                                "Confirmar salida",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                        System.exit(0);
                }
        }

        private void probarConexion() {
                try (Connection connection = Conexion.conectar()) {
                        if (connection != null && !connection.isClosed()) {
                                JOptionPane.showMessageDialog(
                                                this,
                                                "Conexión exitosa con MySQL.\n\nBase de datos: jardineria",
                                                "Conexión exitosa",
                                                JOptionPane.INFORMATION_MESSAGE);
                        }
                } catch (SQLException exception) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        Conexion.diagnosticoError(exception, "MenuMySQL.probarConexion"),
                                        "Error de conexión",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        public static void main(String[] args) {
                SwingUtilities.invokeLater(() -> {
                        try {
                                UIManager.setLookAndFeel(new FlatDarkLaf());
                                new MenuMySQL().setVisible(true);
                        } catch (UnsupportedLookAndFeelException | RuntimeException exception) {
                                JOptionPane.showMessageDialog(null,
                                                Conexion.diagnosticoError(exception, "MenuMySQL.main"),
                                                "No se pudo iniciar Jardinería", JOptionPane.ERROR_MESSAGE);
                        }
                });
        }
}
