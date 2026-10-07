package com.example.starkindustry.ui;

import com.example.starkindustry.dao.CustomerDAO;
import com.example.starkindustry.model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Input UI: manage customers (search, add, update, delete). */
public class CustomerPanel extends JPanel {

    private static final String WALK_IN = "Walk-in Customer";   // protected default customer

    private final CustomerDAO customerDAO = new CustomerDAO();
    private List<Customer> customers = new ArrayList<>();   // same order as the table rows
    private int selectedCustomerId = 0;                     // 0 = nothing selected

    private final JTextField txtSearch = new JTextField(22);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Name", "Phone", "Email", "Address", "Type"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new JTextField(18);
    private final JTextField txtPhone = new JTextField(12);
    private final JTextField txtEmail = new JTextField(18);
    private final JTextField txtAddress = new JTextField(22);
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"Retail", "Garage"});

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        add(buildTop(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        loadTable("");
    }

    // ================= layout =================
    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Customers");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnSearch = new JButton("Search");
        JButton btnAll = new JButton("Show All");
        search.add(new JLabel("Name / phone / email:"));
        search.add(txtSearch);
        search.add(btnSearch);
        search.add(btnAll);

        btnSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));
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
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.setBorder(BorderFactory.createTitledBorder("Customer details  (Garage customers get a discount on invoices)"));

        JPanel fields = new JPanel(new GridBagLayout());
        addField(fields, "Name *", txtName, 0, 0);
        addField(fields, "Phone *", txtPhone, 1, 0);
        addField(fields, "Type *", cmbType, 2, 0);
        addField(fields, "Email", txtEmail, 0, 1);
        addField(fields, "Address", txtAddress, 1, 1);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAdd = new JButton("Add New");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());

        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

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
    private void loadTable(String keyword) {
        try {
            customers = customerDAO.search(keyword);
            tableModel.setRowCount(0);
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                        c.getName(), c.getPhone(),
                        c.getEmail() == null ? "" : c.getEmail(),
                        c.getAddress() == null ? "" : c.getAddress(),
                        c.getCustomerType()
                });
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= customers.size()) {
            return;
        }
        Customer c = customers.get(row);
        selectedCustomerId = c.getCustomerId();
        txtName.setText(c.getName());
        txtPhone.setText(c.getPhone());
        txtEmail.setText(c.getEmail() == null ? "" : c.getEmail());
        txtAddress.setText(c.getAddress() == null ? "" : c.getAddress());
        cmbType.setSelectedItem(c.getCustomerType());
    }

    // ================= button actions =================
    private void addCustomer() {
        Customer c = readForm();
        if (c == null) {
            return;
        }
        try {
            customerDAO.insert(c);
            JOptionPane.showMessageDialog(this, "Customer added successfully.");
            clearForm();
            loadTable(txtSearch.getText().trim());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateCustomer() {
        if (selectedCustomerId == 0) {
            showError("Select a customer from the table first.");
            return;
        }
        if (isWalkIn()) {
            showError("The Walk-in Customer is a system record and cannot be changed.");
            return;
        }
        Customer c = readForm();
        if (c == null) {
            return;
        }
        c.setCustomerId(selectedCustomerId);
        try {
            customerDAO.update(c);
            JOptionPane.showMessageDialog(this, "Customer updated successfully.");
            clearForm();
            loadTable(txtSearch.getText().trim());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteCustomer() {
        if (selectedCustomerId == 0) {
            showError("Select a customer from the table first.");
            return;
        }
        if (isWalkIn()) {
            showError("The Walk-in Customer is a system record and cannot be deleted.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete customer " + txtName.getText() + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            customerDAO.delete(selectedCustomerId);
            clearForm();
            loadTable(txtSearch.getText().trim());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean isWalkIn() {
        return WALK_IN.equals(txtName.getText().trim());
    }

    private void clearForm() {
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        cmbType.setSelectedIndex(0);
        table.clearSelection();
        selectedCustomerId = 0;   // reset after clearSelection
    }

    // ================= validation =================
    private Customer readForm() {
        StringBuilder errors = new StringBuilder();

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty()) {
            errors.append("- Name is required\n");
        } else if (name.length() > 100) {
            errors.append("- Name can have at most 100 characters\n");
        }
        if (phone.isEmpty()) {
            errors.append("- Phone is required\n");
        } else if (!phone.matches("\\d{10}")) {
            errors.append("- Phone must be exactly 10 digits (e.g. 0771234567)\n");
        }
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            errors.append("- Email format is not valid\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Please fix these", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Customer c = new Customer();
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email.isEmpty() ? null : email);
        c.setAddress(address.isEmpty() ? null : address);
        c.setCustomerType((String) cmbType.getSelectedItem());
        return c;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}