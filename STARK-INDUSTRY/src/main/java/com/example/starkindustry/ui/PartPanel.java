package com.example.starkindustry.ui;

import com.example.starkindustry.dao.PartDAO;
import com.example.starkindustry.model.Category;
import com.example.starkindustry.model.Part;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class PartPanel extends JPanel {

    private final PartDAO partDAO = new PartDAO();
    private List<Part> parts = new ArrayList<>();   // same order as the table rows
    private int selectedPartId = 0;                 // 0 = nothing selected

    // search + table
    private final JTextField txtSearch = new JTextField(22);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Part No", "Name", "Category", "Brand", "Vehicle", "Cost", "Price", "Stock", "Reorder"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // form fields
    private final JTextField txtPartNo = new JTextField(12);
    private final JTextField txtName = new JTextField(16);
    private final JComboBox<Category> cmbCategory = new JComboBox<>();
    private final JTextField txtBrand = new JTextField(12);
    private final JTextField txtVehicle = new JTextField(16);
    private final JTextField txtCost = new JTextField(10);
    private final JTextField txtPrice = new JTextField(10);
    private final JTextField txtStock = new JTextField(10);
    private final JTextField txtReorder = new JTextField(10);

    public PartPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        add(buildTop(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        loadCategories();
        loadTable("");
    }

    // ================= layout =================
    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Spare Parts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnSearch = new JButton("Search");
        JButton btnAll = new JButton("Show All");
        search.add(new JLabel("Part no / name / brand / vehicle:"));
        search.add(txtSearch);
        search.add(btnSearch);
        search.add(btnAll);

        btnSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));   // Enter key
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            loadTable("");
        });

        top.add(title, BorderLayout.WEST);
        top.add(search, BorderLayout.EAST);
        return top;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // low stock rows (stock <= reorder level) are shown in red
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (selected) {
                    c.setForeground(t.getSelectionForeground());
                } else if (row < parts.size() && parts.get(row).getStockQty() <= parts.get(row).getReorderLevel()) {
                    c.setForeground(new Color(198, 40, 40));
                } else {
                    c.setForeground(t.getForeground());
                }
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.setBorder(BorderFactory.createTitledBorder("Part details  (red rows in the table = low stock)"));

        JPanel fields = new JPanel(new GridBagLayout());
        addField(fields, "Part No *", txtPartNo, 0, 0);
        addField(fields, "Name *", txtName, 1, 0);
        addField(fields, "Category *", cmbCategory, 2, 0);
        addField(fields, "Brand *", txtBrand, 0, 1);
        addField(fields, "Vehicle Model", txtVehicle, 1, 1);
        addField(fields, "Cost Price *", txtCost, 2, 1);
        addField(fields, "Unit Price *", txtPrice, 0, 2);
        addField(fields, "Stock Qty *", txtStock, 1, 2);
        addField(fields, "Reorder Level *", txtReorder, 2, 2);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAdd = new JButton("Add New");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        btnAdd.addActionListener(e -> addPart());
        btnUpdate.addActionListener(e -> updatePart());
        btnDelete.addActionListener(e -> deletePart());
        btnClear.addActionListener(e -> clearForm());

        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    /** Puts "label + field" at a grid position (col 0..2, row 0..2). */
    private void addField(JPanel panel, String label, JComponent field, int col, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = row;
        c.gridx = col * 2;
        panel.add(new JLabel(label), c);
        c.gridx = col * 2 + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(field, c);
    }

    // ================= loading data =================
    private void loadCategories() {
        try {
            for (Category c : partDAO.getAllCategories()) {
                cmbCategory.addItem(c);
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadTable(String keyword) {
        try {
            parts = partDAO.search(keyword);
            tableModel.setRowCount(0);
            for (Part p : parts) {
                tableModel.addRow(new Object[]{
                        p.getPartNo(), p.getName(), p.getCategoryName(), p.getBrand(),
                        p.getVehicleModel() == null ? "" : p.getVehicleModel(),
                        String.format("%.2f", p.getCostPrice()),
                        String.format("%.2f", p.getUnitPrice()),
                        String.valueOf(p.getStockQty()),
                        String.valueOf(p.getReorderLevel())
                });
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= parts.size()) {
            return;
        }
        Part p = parts.get(row);
        selectedPartId = p.getPartId();
        txtPartNo.setText(p.getPartNo());
        txtName.setText(p.getName());
        txtBrand.setText(p.getBrand());
        txtVehicle.setText(p.getVehicleModel() == null ? "" : p.getVehicleModel());
        txtCost.setText(String.valueOf(p.getCostPrice()));
        txtPrice.setText(String.valueOf(p.getUnitPrice()));
        txtStock.setText(String.valueOf(p.getStockQty()));
        txtReorder.setText(String.valueOf(p.getReorderLevel()));
        for (int i = 0; i < cmbCategory.getItemCount(); i++) {
            if (cmbCategory.getItemAt(i).getCategoryId() == p.getCategoryId()) {
                cmbCategory.setSelectedIndex(i);
                break;
            }
        }
    }

    // ================= button actions =================
    private void addPart() {
        Part p = readForm();
        if (p == null) {
            return;
        }
        try {
            partDAO.insert(p);
            JOptionPane.showMessageDialog(this, "Part added successfully.");
            clearForm();
            loadTable(txtSearch.getText().trim());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updatePart() {
        if (selectedPartId == 0) {
            showError("Select a part from the table first.");
            return;
        }
        Part p = readForm();
        if (p == null) {
            return;
        }
        p.setPartId(selectedPartId);
        try {
            partDAO.update(p);
            JOptionPane.showMessageDialog(this, "Part updated successfully.");
            clearForm();
            loadTable(txtSearch.getText().trim());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void deletePart() {
        if (selectedPartId == 0) {
            showError("Select a part from the table first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete part " + txtPartNo.getText() + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            partDAO.delete(selectedPartId);
            clearForm();
            loadTable(txtSearch.getText().trim());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearForm() {
        selectedPartId = 0;
        txtPartNo.setText("");
        txtName.setText("");
        txtBrand.setText("");
        txtVehicle.setText("");
        txtCost.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        txtReorder.setText("");
        if (cmbCategory.getItemCount() > 0) {
            cmbCategory.setSelectedIndex(0);
        }
        table.clearSelection();
        selectedPartId = 0;   // clearSelection can trigger the listener, so reset again
    }

    // ================= validation =================
    /** Reads and validates the form. Returns null (after showing all errors) if anything is wrong. */
    private Part readForm() {
        StringBuilder errors = new StringBuilder();

        String partNo = txtPartNo.getText().trim();
        String name = txtName.getText().trim();
        String brand = txtBrand.getText().trim();
        String vehicle = txtVehicle.getText().trim();
        Category category = (Category) cmbCategory.getSelectedItem();

        if (partNo.isEmpty()) {
            errors.append("- Part No is required\n");
        } else if (partNo.length() > 20) {
            errors.append("- Part No can have at most 20 characters\n");
        }
        if (name.isEmpty()) {
            errors.append("- Name is required\n");
        }
        if (category == null) {
            errors.append("- Category is required\n");
        }
        if (brand.isEmpty()) {
            errors.append("- Brand is required\n");
        }

        double cost = 0;
        double price = 0;
        int stock = 0;
        int reorder = 0;

        try {
            cost = Double.parseDouble(txtCost.getText().trim());
            if (cost < 0) {
                errors.append("- Cost price cannot be negative\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Cost price must be a number\n");
        }
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            if (price < 0) {
                errors.append("- Unit price cannot be negative\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Unit price must be a number\n");
        }
        try {
            stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) {
                errors.append("- Stock qty cannot be negative\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Stock qty must be a whole number\n");
        }
        try {
            reorder = Integer.parseInt(txtReorder.getText().trim());
            if (reorder < 0) {
                errors.append("- Reorder level cannot be negative\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Reorder level must be a whole number\n");
        }
        if (errors.length() == 0 && price < cost) {
            errors.append("- Unit price cannot be lower than the cost price\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Please fix these", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Part p = new Part();
        p.setPartNo(partNo);
        p.setName(name);
        p.setCategoryId(category.getCategoryId());
        p.setBrand(brand);
        p.setVehicleModel(vehicle.isEmpty() ? null : vehicle);
        p.setCostPrice(cost);
        p.setUnitPrice(price);
        p.setStockQty(stock);
        p.setReorderLevel(reorder);
        return p;
    }

    /** Reloads the table (called every time this screen is shown, because invoices change stock). */
    public void refresh() {
        loadTable(txtSearch.getText().trim());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}