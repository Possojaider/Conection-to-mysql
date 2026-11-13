package com.conectiontomysql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.conectiontomysql.application.model.EntityField;
import com.conectiontomysql.application.model.EntityOption;
import com.conectiontomysql.application.model.EntityTable;
import com.conectiontomysql.application.model.EntityValue;
import com.conectiontomysql.application.ports.in.EntityManagementUseCase;

public class EntityCrudFrame extends JFrame {
    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final int PREVIEW_WIDTH = 220;
    private static final int PREVIEW_HEIGHT = 180;
    private static final List<String> ALLOWED_TABLES = List.of(
            "oficina", "empleado", "cliente", "gama_producto",
            "producto", "pedido", "detalle_pedido", "pago");

    private final String tableName;
    private final EntityManagementUseCase entityManagement = ApplicationFactory.createEntityManagementUseCase();
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField search = new JTextField(24);
    private final JComboBox<String> filterColumn = new JComboBox<>();
    private final JTextField filterText = new JTextField(14);
    private final JTextField paymentFrom = new JTextField(10);
    private final JTextField paymentTo = new JTextField(10);
    private final JTextField officeCode = new JTextField(8);
    private final JTextField maximumStock = new JTextField(4);
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JButton createButton = new JButton("Nuevo");
    private final JButton editButton = new JButton("Editar");
    private final JButton deleteButton = new JButton("Eliminar");
    private final JButton refreshButton = new JButton("Actualizar");
    private final ImagePreview imagePreview = new ImagePreview(
            PREVIEW_WIDTH, PREVIEW_HEIGHT, "Selecciona una gama para ver su imagen");
    private List<EntityField> columns = List.of();
    private List<String> primaryKey = List.of();

    public EntityCrudFrame(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Entidad no permitida: " + tableName);
        }
        this.tableName = tableName;
        setTitle("Gestión de " + tableName);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        JLabel title = new JLabel("Administración de " + tableName.replace('_', ' '));
        title.setFont(title.getFont().deriveFont(22f));
        header.add(title, BorderLayout.WEST);
        search.putClientProperty("JTextField.placeholderText", "Buscar en todos los campos");
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                applyFilters();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Buscar:"));
        searchPanel.add(search);
        searchPanel.add(new JLabel("Filtrar por:"));
        searchPanel.add(filterColumn);
        searchPanel.add(filterText);
        JPanel controls = new JPanel(new BorderLayout());
        controls.add(searchPanel, BorderLayout.NORTH);
        JPanel businessFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        if ("pago".equals(tableName)) {
            businessFilters.add(new JLabel("Fecha desde (AAAA-MM-DD):"));
            businessFilters.add(paymentFrom);
            businessFilters.add(new JLabel("hasta:"));
            businessFilters.add(paymentTo);
        } else if ("empleado".equals(tableName)) {
            businessFilters.add(new JLabel("Código de oficina:"));
            businessFilters.add(officeCode);
        } else if ("producto".equals(tableName)) {
            businessFilters.add(new JLabel("Stock máximo (opcional):"));
            businessFilters.add(maximumStock);
        }
        if (businessFilters.getComponentCount() > 0) {
            controls.add(businessFilters, BorderLayout.SOUTH);
        }
        header.add(controls, BorderLayout.SOUTH);
        filterText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void apply() {
                applyFilters();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                apply();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                apply();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                apply();
            }
        });
        filterColumn.addActionListener(event -> applyFilters());
        listenForFilterChanges(paymentFrom);
        listenForFilterChanges(paymentTo);
        listenForFilterChanges(officeCode);
        listenForFilterChanges(maximumStock);

        createButton.addActionListener(event -> createRecord());
        editButton.addActionListener(event -> editRecord());
        deleteButton.addActionListener(event -> deleteRecord());
        refreshButton.addActionListener(event -> loadRecords());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        actions.add(createButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        if ("gama_producto".equals(tableName)) {
            content.add(imagePreview, BorderLayout.EAST);
            table.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    showSelectedImage();
                }
            });
        }

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        loadRecords();
    }

    private void loadRecords() {
        setActionsEnabled(false);
        sorter.setRowFilter(null);
        model.setRowCount(0);
        model.setColumnCount(0);
        new javax.swing.SwingWorker<EntityTable, Void>() {
            @Override
            protected EntityTable doInBackground() {
                return entityManagement.load(tableName);
            }

            @Override
            protected void done() {
                try {
                    EntityTable loaded = get();
                    columns = loaded.fields();
                    primaryKey = loaded.primaryKey();
                    columns.forEach(column -> model.addColumn(column.name()));
                    loaded.rows().forEach(model::addRow);

                    String selectedFilter = (String) filterColumn.getSelectedItem();
                    filterColumn.removeAllItems();
                    filterColumn.addItem("Todas las columnas");
                    columns.forEach(column -> filterColumn.addItem(column.name()));
                    if (selectedFilter != null) {
                        filterColumn.setSelectedItem(selectedFilter);
                    }
                    applyFilters();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(EntityCrudFrame.this,
                            "Se interrumpió la carga de registros.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    JOptionPane.showMessageDialog(EntityCrudFrame.this,
                            Conexion.diagnosticoError(cause, "EntityCrudFrame.loadRecords"),
                            "Error de base de datos", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setActionsEnabled(true);
                }
            }
        }.execute();
    }

    private void createRecord() {
        List<EntityField> writable = columns.stream().filter(column -> !column.autoIncrement()).toList();
        showForm("Nuevo registro", writable, null, values -> {
            runDatabaseOperation("No se pudo crear el registro.", () ->
                    entityManagement.insert(tableName, toEntityValues(values)));
        });
    }

    private void editRecord() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para editar.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        if (primaryKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La tabla no define clave primaria; no se puede actualizar de forma segura.");
            return;
        }

        List<EntityField> editable = columns.stream()
                .filter(column -> !primaryKey.contains(column.name()))
                .toList();
        showForm("Editar registro", editable, modelRow, values -> {
            List<EntityValue> keyValues = primaryKey.stream()
                    .map(key -> entityValue(columnInfo(key), model.getValueAt(modelRow, columnIndex(key))))
                    .toList();
            runDatabaseOperation("No se pudo actualizar el registro.", () ->
                    entityManagement.update(tableName, toEntityValues(values), keyValues));
        });
    }

    private void deleteRecord() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para eliminar.");
            return;
        }
        if (primaryKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La tabla no define clave primaria; no se puede eliminar de forma segura.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el registro seleccionado? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(selected);
        List<EntityValue> keyValues = primaryKey.stream()
                .map(key -> entityValue(columnInfo(key), model.getValueAt(modelRow, columnIndex(key))))
                .toList();
        runDatabaseOperation("No se pudo eliminar el registro. Puede tener relaciones activas.",
                () -> entityManagement.delete(tableName, keyValues));
    }

    private void showForm(String title, List<EntityField> fields, Integer modelRow, FormSubmit submit) {
        if (fields.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay campos editables.");
            return;
        }
        JPanel form = new JPanel(new GridBagLayout());
        List<FormField> controls = new ArrayList<>();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;

        int row = 0;
        try {
            for (EntityField column : fields) {
                constraints.gridx = 0;
                constraints.gridy = row;
                form.add(new JLabel(column.name()), constraints);
                constraints.gridx = 1;
                constraints.weightx = 1;

                FormField control = createField(column, modelRow == null ? null
                        : model.getValueAt(modelRow, columnIndex(column.name())));
                controls.add(control);
                form.add(control.component(), constraints);
                row++;
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    Conexion.diagnosticoError(e, "EntityCrudFrame.showForm"),
                    "No se pudo abrir el formulario", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this,
                    Conexion.diagnosticoError(e, "EntityCrudFrame.createField"),
                    "No se pudo abrir el formulario", JOptionPane.ERROR_MESSAGE);
            return;
        }
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weighty = 1;
        form.add(new JPanel(), constraints);

        int answer = JOptionPane.showConfirmDialog(this, new JScrollPane(form), title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            List<BoundValue> values = new ArrayList<>();
            for (FormField control : controls) {
                values.add(new BoundValue(control.column(), control.value()));
            }
            validateBusinessFields(values);
            submit.execute(values);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    Conexion.diagnosticoError(e, "EntityCrudFrame.validateBusinessFields"),
                    "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private FormField createField(EntityField column, Object currentValue) {
        if (column.foreignKeyTable() != null) {
            JComboBox<EntityOption> combo = new JComboBox<>();
            if (column.nullable()) {
                combo.addItem(new EntityOption(null, "-- Sin asignar --"));
            }
            List<EntityOption> options = entityManagement.loadOptions(
                    column.foreignKeyTable(), column.foreignKeyColumn());
            for (EntityOption option : options) {
                combo.addItem(option);
                if (currentValue != null && option.value() != null
                        && currentValue.toString().equals(option.value().toString())) {
                    combo.setSelectedItem(option);
                }
            }
            return new FormField(column, combo);
        }

        if ("gama_producto".equals(tableName) && "imagen".equals(column.name())) {
            return new FormField(column, new ImageInput(currentValue == null ? "" : currentValue.toString()));
        }

        JTextField field = new JTextField(currentValue == null ? "" : currentValue.toString(), 28);
        return new FormField(column, field);
    }

    private void showSelectedImage() {
        int selected = table.getSelectedRow();
        if (selected < 0 || !columns.stream().anyMatch(column -> "imagen".equals(column.name()))) {
            imagePreview.load(null);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        Object value = model.getValueAt(modelRow, columnIndex("imagen"));
        imagePreview.load(value == null ? null : value.toString());
    }

    private static BufferedImage readImage(String source) throws IOException {
        if (source.regionMatches(true, 0, "http://", 0, 7)
                || source.regionMatches(true, 0, "https://", 0, 8)) {
            URI uri;
            try {
                uri = URI.create(source);
            } catch (IllegalArgumentException exception) {
                throw new IOException("La URL de la imagen no es válida.", exception);
            }
            if (uri.getHost() == null) {
                throw new IOException("La URL de la imagen no contiene un servidor válido.");
            }
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (connection.getContentLengthLong() > MAX_IMAGE_BYTES) {
                throw new IOException("La imagen supera el límite de 10 MB.");
            }
            try (InputStream input = connection.getInputStream()) {
                return decodeImage(input);
            }
        }

        Path path = Path.of(source);
        if (Files.size(path) > MAX_IMAGE_BYTES) {
            throw new IOException("La imagen supera el límite de 10 MB.");
        }
        try (InputStream input = Files.newInputStream(path)) {
            return decodeImage(input);
        }
    }

    private static BufferedImage decodeImage(InputStream input) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > MAX_IMAGE_BYTES) {
                    throw new IOException("La imagen supera el límite de 10 MB.");
                }
                bytes.write(buffer, 0, count);
            }
            try (ImageInputStream imageInput = ImageIO.createImageInputStream(
                    new ByteArrayInputStream(bytes.toByteArray()))) {
                if (imageInput == null || !ImageIO.getImageReaders(imageInput).hasNext()) {
                    throw new IOException("El archivo no contiene un formato de imagen compatible.");
                }
                ImageReader reader = ImageIO.getImageReaders(imageInput).next();
                try {
                    reader.setInput(imageInput, true, true);
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 20_000_000L) {
                        throw new IOException("La resolución de la imagen es demasiado grande.");
                    }
                    return reader.read(0);
                } finally {
                    reader.dispose();
                }
            }
        }
    }

    private List<EntityValue> toEntityValues(List<BoundValue> values) {
        return values.stream().map(value -> entityValue(value.column(), value.value())).toList();
    }

    private EntityValue entityValue(EntityField field, Object value) {
        return new EntityValue(field.name(), field.jdbcType(), value);
    }

    private void runDatabaseOperation(String errorMessage, EntityOperation operation) {
        setActionsEnabled(false);
        new javax.swing.SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return operation.execute();
            }

            @Override
            protected void done() {
                boolean refresh = false;
                try {
                    if (get() == 0) {
                        JOptionPane.showMessageDialog(EntityCrudFrame.this,
                                "No se modificaron registros.");
                    } else {
                        refresh = true;
                        loadRecords();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(EntityCrudFrame.this,
                            "Se interrumpió la operación.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    JOptionPane.showMessageDialog(EntityCrudFrame.this,
                            errorMessage + "\n\n"
                                    + Conexion.diagnosticoError(cause, "EntityCrudFrame.runDatabaseOperation"),
                            "Error de base de datos", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (!refresh) {
                        setActionsEnabled(true);
                    }
                }
            }
        }.execute();
    }

    private void setActionsEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    private Object parseValue(EntityField column, Object rawValue) {
        if (rawValue instanceof EntityOption option) {
            rawValue = option.value();
        }
        if (rawValue == null || rawValue.toString().isBlank()) {
            if (!column.nullable() && !column.autoIncrement()) {
                throw new IllegalArgumentException(column.name() + " es obligatorio.");
            }
            return null;
        }
        String text = rawValue.toString().trim();
        try {
            Object value = switch (column.jdbcType()) {
                case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> Integer.valueOf(text);
                case Types.BIGINT -> Long.valueOf(text);
                case Types.NUMERIC, Types.DECIMAL -> new BigDecimal(text);
                case Types.FLOAT, Types.REAL -> Float.valueOf(text);
                case Types.DOUBLE -> Double.valueOf(text);
                case Types.DATE -> LocalDate.parse(text);
                case Types.BOOLEAN, Types.BIT -> {
                    if (!"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)) {
                        throw new IllegalArgumentException(
                                "El valor booleano debe ser true o false para " + column.name() + ".");
                    }
                    yield Boolean.valueOf(text);
                }
                default -> text;
            };
            validateField(column.name(), value);
            return value;
        } catch (NumberFormatException | java.time.DateTimeException e) {
            throw new IllegalArgumentException("Valor inválido para " + column.name() + ": " + text, e);
        }
    }

    private void validateBusinessFields(List<BoundValue> values) {
        java.util.Map<String, Object> formValues = new java.util.HashMap<>();
        values.forEach(value -> formValues.put(value.column().name(), value.value()));
        if ("pedido".equals(tableName)) {
            LocalDate orderDate = (LocalDate) formValues.getOrDefault("fecha_pedido",
                    currentValue("fecha_pedido"));
            LocalDate expectedDate = (LocalDate) formValues.getOrDefault("fecha_esperada",
                    currentValue("fecha_esperada"));
            LocalDate deliveryDate = (LocalDate) formValues.getOrDefault("fecha_entrega",
                    currentValue("fecha_entrega"));
            if (orderDate != null && expectedDate != null && expectedDate.isBefore(orderDate)) {
                throw new IllegalArgumentException("La fecha esperada no puede ser anterior a la fecha del pedido.");
            }
            if (orderDate != null && deliveryDate != null && deliveryDate.isBefore(orderDate)) {
                throw new IllegalArgumentException("La fecha de entrega no puede ser anterior a la fecha del pedido.");
            }
        }
    }

    private Object currentValue(String columnName) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        Object value = model.getValueAt(modelRow, columnIndex(columnName));
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        return value;
    }

    private void validateField(String columnName, Object value) {
        if (value == null) {
            return;
        }
        if ("cliente".equals(tableName) && "limite_credito".equals(columnName)
                && ((BigDecimal) value).signum() < 0) {
            throw new IllegalArgumentException("El límite de crédito no puede ser negativo.");
        }
        if ("producto".equals(tableName) && "cantidad_en_stock".equals(columnName)
                && ((Number) value).intValue() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if ("producto".equals(tableName) && ("precio_venta".equals(columnName)
                || "precio_proveedor".equals(columnName)) && ((BigDecimal) value).signum() < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos.");
        }
        if ("detalle_pedido".equals(tableName) && "cantidad".equals(columnName)
                && ((Number) value).intValue() <= 0) {
            throw new IllegalArgumentException("La cantidad del detalle debe ser positiva.");
        }
        if (("detalle_pedido".equals(tableName) && "precio_unidad".equals(columnName)
                || "pago".equals(tableName) && "total".equals(columnName))
                && ((BigDecimal) value).signum() < 0) {
            throw new IllegalArgumentException("El importe no puede ser negativo.");
        }
    }

    private void applyFilters() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        String query = search.getText().trim();
        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        }
        String selectedColumn = (String) filterColumn.getSelectedItem();
        String value = filterText.getText().trim();
        if (!value.isEmpty() && selectedColumn != null) {
            if ("Todas las columnas".equals(selectedColumn)) {
                filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(value)));
            } else {
                int index = -1;
                for (int i = 0; i < columns.size(); i++) {
                    if (columns.get(i).name().equals(selectedColumn)) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(value), index));
                }
            }
        }
        if ("pago".equals(tableName)) {
            LocalDate from = parseDateFilter(paymentFrom);
            LocalDate to = parseDateFilter(paymentTo);
            if ((!paymentFrom.getText().isBlank() && from == null)
                    || (!paymentTo.getText().isBlank() && to == null)) {
                return;
            }
            if (from != null && to != null && to.isBefore(from)) {
                paymentTo.setToolTipText("La fecha final no puede ser anterior a la inicial.");
                paymentTo.setForeground(new java.awt.Color(190, 30, 30));
                return;
            }
            if (from != null || to != null) {
                int dateColumn = columnIndex("fecha_pago");
                filters.add(new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        Object raw = entry.getValue(dateColumn);
                        LocalDate date = raw instanceof java.sql.Date sqlDate
                                ? sqlDate.toLocalDate() : LocalDate.parse(raw.toString());
                        return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
                    }
                });
            }
        } else if ("empleado".equals(tableName) && !officeCode.getText().isBlank()) {
            String selectedOffice = officeCode.getText().trim();
            int officeColumn = columnIndex("codigo_oficina");
            filters.add(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return selectedOffice.equalsIgnoreCase(String.valueOf(entry.getValue(officeColumn)));
                }
            });
        } else if ("producto".equals(tableName) && !maximumStock.getText().isBlank()) {
            Integer threshold = parseStockThreshold();
            if (threshold == null) {
                return;
            }
            int stockColumn = columnIndex("cantidad_en_stock");
            filters.add(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return ((Number) entry.getValue(stockColumn)).intValue() <= threshold;
                }
            });
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void listenForFilterChanges(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void apply() {
                applyFilters();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                apply();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                apply();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                apply();
            }
        });
    }

    private LocalDate parseDateFilter(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            field.setToolTipText(null);
            field.setForeground(javax.swing.UIManager.getColor("TextField.foreground"));
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(text);
            field.setToolTipText(null);
            field.setForeground(javax.swing.UIManager.getColor("TextField.foreground"));
            return date;
        } catch (java.time.DateTimeException e) {
            field.setToolTipText("Usa el formato AAAA-MM-DD.");
            field.setForeground(new java.awt.Color(190, 30, 30));
            return null;
        }
    }

    private Integer parseStockThreshold() {
        try {
            int threshold = Integer.parseInt(maximumStock.getText().trim());
            if (threshold < 0) {
                throw new NumberFormatException();
            }
            maximumStock.setToolTipText(null);
            maximumStock.setForeground(javax.swing.UIManager.getColor("TextField.foreground"));
            return threshold;
        } catch (NumberFormatException e) {
            maximumStock.setToolTipText("Escribe un número entero igual o mayor que cero.");
            maximumStock.setForeground(new java.awt.Color(190, 30, 30));
            return null;
        }
    }

    private int columnIndex(String column) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).name().equals(column)) {
                return i;
            }
        }
        throw new IllegalArgumentException("No existe la columna " + column);
    }

    private EntityField columnInfo(String name) {
        return columns.stream().filter(column -> column.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe la columna " + name));
    }

    private record BoundValue(EntityField column, Object value) {
    }

    private final class FormField {
        private final EntityField column;
        private final java.awt.Component component;

        private FormField(EntityField column, java.awt.Component component) {
            this.column = column;
            this.component = component;
        }

        private EntityField column() {
            return column;
        }

        private java.awt.Component component() {
            return component;
        }

        private Object value() {
            Object raw;
            if (component instanceof ImageInput imageInput) {
                raw = imageInput.value();
            } else if (component instanceof JTextField field) {
                raw = field.getText();
            } else {
                raw = ((JComboBox<?>) component).getSelectedItem();
            }
            return parseValue(column, raw);
        }
    }

    private final class ImageInput extends JPanel {
        private final JTextField path = new JTextField(28);
        private final ImagePreview preview = new ImagePreview(180, 130, "Vista previa");
        private final Timer previewTimer;
        private Path selectedFile;
        private boolean settingSelectedFile;

        private ImageInput(String value) {
            super(new BorderLayout(6, 6));
            path.setText(value);

            JButton browse = new JButton("Elegir archivo...");
            browse.addActionListener(event -> chooseImage());
            JPanel input = new JPanel(new BorderLayout(6, 0));
            input.add(path, BorderLayout.CENTER);
            input.add(browse, BorderLayout.EAST);
            add(input, BorderLayout.NORTH);
            add(preview, BorderLayout.CENTER);

            previewTimer = new Timer(350, event -> preview.load(path.getText()));
            previewTimer.setRepeats(false);
            path.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void changed() {
                    if (!settingSelectedFile) {
                        selectedFile = null;
                    }
                    previewTimer.restart();
                }

                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent event) {
                    changed();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent event) {
                    changed();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent event) {
                    changed();
                }
            });
            preview.load(value);
        }

        private void chooseImage() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Imágenes (PNG, JPG, GIF, BMP)", "png", "jpg", "jpeg", "gif", "bmp"));
            if (chooser.showOpenDialog(EntityCrudFrame.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            selectedFile = chooser.getSelectedFile().toPath();
            settingSelectedFile = true;
            path.setText(selectedFile.toString());
            settingSelectedFile = false;
            preview.load(selectedFile.toString());
        }

        private String value() {
            String value = path.getText().trim();
            if (value.isEmpty()) {
                return null;
            }
            if (selectedFile != null) {
                return copySelectedImage();
            }
            if (value.length() > 256) {
                throw new IllegalArgumentException("La ruta o URL de imagen no puede superar 256 caracteres.");
            }
            if (value.regionMatches(true, 0, "http://", 0, 7)
                    || value.regionMatches(true, 0, "https://", 0, 8)) {
                try {
                    if (URI.create(value).getHost() == null) {
                        throw new IllegalArgumentException("La URL de imagen debe comenzar con http:// o https://.");
                    }
                    return value;
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("La URL de imagen debe comenzar con http:// o https://.",
                            exception);
                }
            }
            try {
                if (!Files.isRegularFile(Path.of(value))) {
                    throw new IllegalArgumentException("No se encontró el archivo de imagen indicado.");
                }
                readImage(value);
                return value;
            } catch (IOException exception) {
                throw new IllegalArgumentException("No se pudo leer la imagen indicada: "
                        + exception.getMessage(), exception);
            }
        }

        private String copySelectedImage() {
            String filename = selectedFile.getFileName().toString();
            int extensionStart = filename.lastIndexOf('.');
            String extension = extensionStart < 0 ? "" : filename.substring(extensionStart).toLowerCase();
            if (!List.of(".png", ".jpg", ".jpeg", ".gif", ".bmp").contains(extension)) {
                throw new IllegalArgumentException("El archivo debe ser PNG, JPG, GIF o BMP.");
            }
            try {
                readImage(selectedFile.toString());
                Path directory = Path.of("imagenes", "gamas");
                Files.createDirectories(directory);
                Path destination = directory.resolve(UUID.randomUUID() + extension);
                Files.copy(selectedFile, destination);
                return directory.resolve(destination.getFileName()).toString().replace('\\', '/');
            } catch (IOException exception) {
                throw new IllegalArgumentException("No se pudo guardar la imagen en imagenes/gamas: "
                        + exception.getMessage(), exception);
            }
        }
    }

    private static final class ImagePreview extends JPanel {
        private final JLabel label = new JLabel("", JLabel.CENTER);
        private final int width;
        private final int height;
        private int requestId;

        private ImagePreview(int width, int height, String placeholder) {
            super(new BorderLayout());
            this.width = width;
            this.height = height;
            setBorder(BorderFactory.createTitledBorder("Vista previa"));
            setPreferredSize(new Dimension(width + 20, height + 45));
            label.setPreferredSize(new Dimension(width, height));
            label.setText(placeholder);
            add(label, BorderLayout.CENTER);
        }

        private void load(String source) {
            int currentRequest = ++requestId;
            label.setIcon(null);
            if (source == null || source.isBlank()) {
                label.setText("Sin imagen");
                label.setToolTipText(null);
                return;
            }
            label.setText("Cargando imagen...");
            new SwingWorker<BufferedImage, Void>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    return readImage(source.trim());
                }

                @Override
                protected void done() {
                    if (currentRequest != requestId) {
                        return;
                    }
                    try {
                        BufferedImage image = get();
                        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        label.setIcon(new ImageIcon(scaled));
                        label.setText(null);
                        label.setToolTipText(source);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        showLoadError("Se interrumpió la carga de la imagen.");
                    } catch (ExecutionException exception) {
                        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                        showLoadError(cause.getMessage() == null
                                ? "No se pudo cargar la imagen." : cause.getMessage());
                    }
                }
            }.execute();
        }

        private void showLoadError(String message) {
            label.setIcon(null);
            label.setText("<html><div style='text-align:center;width:180px'>No se pudo cargar la imagen</div></html>");
            label.setToolTipText(message);
        }
    }

    @FunctionalInterface
    private interface FormSubmit {
        void execute(List<BoundValue> values);
    }

    @FunctionalInterface
    private interface EntityOperation {
        int execute();
    }
}
