//package com.example.starkindustry.ui;
//
//import com.example.starkindustry.dao.CustomerDAO;
//import com.example.starkindustry.dao.InvoiceDAO;
//import com.example.starkindustry.dao.PartDAO;
//import com.example.starkindustry.model.Customer;
//import com.example.starkindustry.model.Invoice;
//import com.example.starkindustry.model.InvoiceItem;
//import com.example.starkindustry.model.Part;
//import com.example.starkindustry.model.User;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
///** Transaction UI: create a sales invoice (the "till" screen). */
//public class InvoicePanel extends JPanel {
//
//    private static final String WALK_IN = "Walk-in Customer";
//    private static final double GARAGE_DISCOUNT = 5;   // % automatically given to Garage customers
//    private static final double MAX_DISCOUNT = 20;     // % the cashier can never exceed
//
//    private final CustomerDAO customerDAO = new CustomerDAO();
//    private final PartDAO partDAO = new PartDAO();
//    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
//    private final User loggedUser;
//
//    private final List<InvoiceItem> cart = new ArrayList<>();   // same order as the table rows
//    private boolean loadingCustomers = false;
//
//    // invoice header
//    private final JComboBox<Customer> cmbCustomer = new JComboBox<>();
//    private final JComboBox<String> cmbPayment = new JComboBox<>(new String[]{"Cash", "Card", "Credit"});
//
//    // part picker
//    private final JTextField txtFind = new JTextField(12);
//    private final JComboBox<Part> cmbPart = new JComboBox<>();
//    private final JLabel lblPartInfo = new JLabel(" ");
//    private final JTextField txtQty = new JTextField("1", 4);
//
//    // invoice lines
//    private final DefaultTableModel tableModel = new DefaultTableModel(
//            new String[]{"Part No", "Part", "Qty", "Unit Price", "Line Total"}, 0) {
//        @Override
//        public boolean isCellEditable(int row, int column) {
//            return false;
//        }
//    };
//    private final JTable table = new JTable(tableModel);
//
//    // totals
//    private final JTextField txtDiscountPct = new JTextField("0", 5);
//    private final JLabel lblSubTotal = new JLabel("0.00");
//    private final JLabel lblDiscount = new JLabel("0.00");
//    private final JLabel lblTotal = new JLabel("0.00");
//
//    public InvoicePanel(User loggedUser) {
//        this.loggedUser = loggedUser;
//        setLayout(new BorderLayout(0, 12));
//        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
//
//        add(buildTop(), BorderLayout.NORTH);
//        add(buildTable(), BorderLayout.CENTER);
//        add(buildBottom(), BorderLayout.SOUTH);
//
//        refresh();
//    }
//
//    /** Reloads customers and parts (called every time this screen is shown). */
//    public void refresh() {
//        loadCustomers();
//        loadParts(txtFind.getText().trim());
//    }
//
//    // ================= layout =================
//    private JPanel buildTop() {
//        JPanel top = new JPanel();
//        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
//
//        JLabel title = new JLabel("New Invoice");
//        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
//        title.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
//        header.setBorder(BorderFactory.createTitledBorder("Invoice details"));
//        header.setAlignmentX(Component.LEFT_ALIGNMENT);
//        cmbCustomer.setPreferredSize(new Dimension(220, 28));
//        header.add(new JLabel("Customer *"));
//        header.add(cmbCustomer);
//        header.add(new JLabel("Payment *"));
//        header.add(cmbPayment);
//        header.add(new JLabel("Date: " + LocalDate.now()));
//
//        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
//        picker.setBorder(BorderFactory.createTitledBorder("Add part"));
//        picker.setAlignmentX(Component.LEFT_ALIGNMENT);
//        JButton btnFind = new JButton("Find");
//        JButton btnAdd = new JButton("Add to Invoice");
//        cmbPart.setPreferredSize(new Dimension(320, 28));
//        picker.add(new JLabel("Find:"));
//        picker.add(txtFind);
//        picker.add(btnFind);
//        picker.add(cmbPart);
//        picker.add(new JLabel("Qty"));
//        picker.add(txtQty);
//        picker.add(btnAdd);
//
//        lblPartInfo.setForeground(Color.GRAY);
//        lblPartInfo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
//        lblPartInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        top.add(title);
//        top.add(Box.createVerticalStrut(8));
//        top.add(header);
//        top.add(picker);
//        top.add(lblPartInfo);
//
//        // events
//        btnFind.addActionListener(e -> loadParts(txtFind.getText().trim()));
//        txtFind.addActionListener(e -> loadParts(txtFind.getText().trim()));
//        cmbPart.addActionListener(e -> showPartInfo());
//        btnAdd.addActionListener(e -> addToInvoice());
//        txtQty.addActionListener(e -> addToInvoice());
//        cmbCustomer.addActionListener(e -> customerChanged());
//        return top;
//    }
//
//    private JScrollPane buildTable() {
//        table.setRowHeight(26);
//        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        table.getTableHeader().setReorderingAllowed(false);
//        return new JScrollPane(table);
//    }
//
//    private JPanel buildBottom() {
//        JPanel bottom = new JPanel(new BorderLayout());
//
//        JButton btnRemove = new JButton("Remove Selected Line");
//        btnRemove.addActionListener(e -> removeSelected());
//        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        left.add(btnRemove);
//
//        // totals block
//        JPanel totals = new JPanel(new GridBagLayout());
//        GridBagConstraints c = new GridBagConstraints();
//        c.insets = new Insets(3, 8, 3, 8);
//        c.anchor = GridBagConstraints.EAST;
//
//        c.gridy = 0; c.gridx = 0; totals.add(new JLabel("Sub Total (Rs.)"), c);
//        c.gridx = 1; totals.add(lblSubTotal, c);
//
//        JPanel pctPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
//        pctPanel.add(new JLabel("Discount %"));
//        pctPanel.add(txtDiscountPct);
//        c.gridy = 1; c.gridx = 0; totals.add(pctPanel, c);
//        c.gridx = 1; totals.add(lblDiscount, c);
//
//        JLabel totalCaption = new JLabel("TOTAL (Rs.)");
//        totalCaption.setFont(totalCaption.getFont().deriveFont(Font.BOLD, 16f));
//        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 20f));
//        lblTotal.setForeground(new Color(46, 125, 50));
//        c.gridy = 2; c.gridx = 0; totals.add(totalCaption, c);
//        c.gridx = 1; totals.add(lblTotal, c);
//
//        JButton btnSave = new JButton("Save Invoice");
//        JButton btnClear = new JButton("Clear");
//        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
//        buttons.add(btnClear);
//        buttons.add(btnSave);
//        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; totals.add(buttons, c);
//
//        btnSave.addActionListener(e -> saveInvoice());
//        btnClear.addActionListener(e -> clearInvoice());
//        txtDiscountPct.addActionListener(e -> recalcTotals());
//        txtDiscountPct.addFocusListener(new java.awt.event.FocusAdapter() {
//            @Override
//            public void focusLost(java.awt.event.FocusEvent e) {
//                recalcTotals();
//            }
//        });
//
//        bottom.add(left, BorderLayout.WEST);
//        bottom.add(totals, BorderLayout.EAST);
//        return bottom;
//    }
//
//    // ================= loading data =================
//    private void loadCustomers() {
//        try {
//            Customer previous = (Customer) cmbCustomer.getSelectedItem();
//            loadingCustomers = true;
//            cmbCustomer.removeAllItems();
//            Customer toSelect = null;
//            for (Customer c : customerDAO.getAll()) {
//                cmbCustomer.addItem(c);
//                if (previous != null && c.getCustomerId() == previous.getCustomerId()) {
//                    toSelect = c;
//                } else if (toSelect == null && previous == null && WALK_IN.equals(c.getName())) {
//                    toSelect = c;   // default customer
//                }
//            }
//            if (toSelect != null) {
//                cmbCustomer.setSelectedItem(toSelect);
//            }
//            loadingCustomers = false;
//            customerChanged();
//        } catch (RuntimeException ex) {
//            loadingCustomers = false;
//            showError(ex.getMessage());
//        }
//    }
//
//    private void loadParts(String keyword) {
//        try {
//            cmbPart.removeAllItems();
//            for (Part p : partDAO.search(keyword)) {
//                cmbPart.addItem(p);
//            }
//            showPartInfo();
//        } catch (RuntimeException ex) {
//            showError(ex.getMessage());
//        }
//    }
//
//    private void showPartInfo() {
//        Part p = (Part) cmbPart.getSelectedItem();
//        if (p == null) {
//            lblPartInfo.setText("No parts found");
//            return;
//        }
//        lblPartInfo.setText(String.format("%s  |  Brand: %s  |  Price: Rs. %,.2f  |  In stock: %d",
//                p.getName(), p.getBrand(), p.getUnitPrice(), p.getStockQty()));
//    }
//
//    /** Garage customers automatically get the garage discount. */
//    private void customerChanged() {
//        if (loadingCustomers) {
//            return;
//        }
//        Customer c = (Customer) cmbCustomer.getSelectedItem();
//        boolean garage = c != null && "Garage".equals(c.getCustomerType());
//        txtDiscountPct.setText(garage ? String.valueOf((int) GARAGE_DISCOUNT) : "0");
//        recalcTotals();
//    }
//
//    // ================= invoice lines =================
//    private void addToInvoice() {
//        Part p = (Part) cmbPart.getSelectedItem();
//        if (p == null) {
//            showWarning("Select a part first.");
//            return;
//        }
//        int qty;
//        try {
//            qty = Integer.parseInt(txtQty.getText().trim());
//        } catch (NumberFormatException e) {
//            showWarning("Quantity must be a whole number.");
//            return;
//        }
//        if (qty <= 0) {
//            showWarning("Quantity must be greater than zero.");
//            return;
//        }
//        InvoiceItem existing = findInCart(p.getPartId());
//        int alreadyOnInvoice = existing == null ? 0 : existing.getQuantity();
//        if (alreadyOnInvoice + qty > p.getStockQty()) {
//            showWarning("Not enough stock for " + p.getPartNo() + ".\nIn stock: " + p.getStockQty()
//                    + ", already on this invoice: " + alreadyOnInvoice);
//            return;
//        }
//
//        if (existing != null) {
//            existing.setQuantity(existing.getQuantity() + qty);
//            existing.setLineTotal(round2(existing.getQuantity() * existing.getUnitPrice()));
//        } else {
//            InvoiceItem item = new InvoiceItem();
//            item.setPartId(p.getPartId());
//            item.setPartNo(p.getPartNo());
//            item.setPartName(p.getName());
//            item.setQuantity(qty);
//            item.setUnitPrice(p.getUnitPrice());
//            item.setLineTotal(round2(qty * p.getUnitPrice()));
//            cart.add(item);
//        }
//        txtQty.setText("1");
//        refreshCartTable();
//    }
//
//    private InvoiceItem findInCart(int partId) {
//        for (InvoiceItem item : cart) {
//            if (item.getPartId() == partId) {
//                return item;
//            }
//        }
//        return null;
//    }
//
//    private void removeSelected() {
//        int row = table.getSelectedRow();
//        if (row < 0) {
//            showWarning("Select a line in the table first.");
//            return;
//        }
//        cart.remove(row);
//        refreshCartTable();
//    }
//
//    private void refreshCartTable() {
//        tableModel.setRowCount(0);
//        for (InvoiceItem item : cart) {
//            tableModel.addRow(new Object[]{
//                    item.getPartNo(), item.getPartName(), item.getQuantity(),
//                    String.format("%,.2f", item.getUnitPrice()),
//                    String.format("%,.2f", item.getLineTotal())
//            });
//        }
//        recalcTotals();
//    }
//
//    // ================= totals =================
//    private double cartSubTotal() {
//        double sum = 0;
//        for (InvoiceItem item : cart) {
//            sum += item.getLineTotal();
//        }
//        return round2(sum);
//    }
//
//    private void recalcTotals() {
//        double subTotal = cartSubTotal();
//        double pct = 0;
//        try {
//            pct = Double.parseDouble(txtDiscountPct.getText().trim());
//        } catch (NumberFormatException e) {
//            pct = 0;   // invalid values are reported when saving
//        }
//        double discount = round2(subTotal * pct / 100.0);
//        lblSubTotal.setText(String.format("%,.2f", subTotal));
//        lblDiscount.setText(String.format("- %,.2f", discount));
//        lblTotal.setText(String.format("%,.2f", subTotal - discount));
//    }
//
//    private double round2(double value) {
//        return Math.round(value * 100.0) / 100.0;
//    }
//
//    // ================= save =================
//    private void saveInvoice() {
//        StringBuilder errors = new StringBuilder();
//
//        Customer customer = (Customer) cmbCustomer.getSelectedItem();
//        String payment = (String) cmbPayment.getSelectedItem();
//
//        if (customer == null) {
//            errors.append("- Select a customer\n");
//        }
//        if (cart.isEmpty()) {
//            errors.append("- Add at least one part to the invoice\n");
//        }
//        double pct = 0;
//        try {
//            pct = Double.parseDouble(txtDiscountPct.getText().trim());
//            if (pct < 0 || pct > MAX_DISCOUNT) {
//                errors.append("- Discount must be between 0 and " + (int) MAX_DISCOUNT + " %\n");
//            }
//        } catch (NumberFormatException e) {
//            errors.append("- Discount must be a number\n");
//        }
//        if (customer != null && WALK_IN.equals(customer.getName()) && "Credit".equals(payment)) {
//            errors.append("- Credit sales are not allowed for the Walk-in Customer\n");
//        }
//
//        if (errors.length() > 0) {
//            JOptionPane.showMessageDialog(this, errors.toString(), "Please fix these", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        double subTotal = cartSubTotal();
//        double discount = round2(subTotal * pct / 100.0);
//
//        Invoice inv = new Invoice();
//        inv.setCustomerId(customer.getCustomerId());
//        inv.setUserId(loggedUser.getUserId());
//        inv.setInvoiceDate(LocalDate.now());
//        inv.setSubTotal(subTotal);
//        inv.setDiscount(discount);
//        inv.setTotalAmount(round2(subTotal - discount));
//        inv.setPaymentMethod(payment);
//        inv.setItems(new ArrayList<>(cart));
//
//        try {
//            int invoiceId = invoiceDAO.save(inv);
//            JOptionPane.showMessageDialog(this, String.format(
//                    "Invoice #%d saved.%nTotal: Rs. %,.2f", invoiceId, inv.getTotalAmount()));
//            clearInvoice();
//            loadParts(txtFind.getText().trim());   // stock has changed
//        } catch (RuntimeException ex) {
//            showError(ex.getMessage());
//        }
//    }
//
//    private void clearInvoice() {
//        cart.clear();
//        txtQty.setText("1");
//        cmbPayment.setSelectedIndex(0);
//        refreshCartTable();
//        customerChanged();
//    }
//
//    private void showWarning(String message) {
//        JOptionPane.showMessageDialog(this, message, "Check this", JOptionPane.WARNING_MESSAGE);
//    }
//
//    private void showError(String message) {
//        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
//    }
//}

package com.example.starkindustry.ui;

import com.example.starkindustry.dao.CustomerDAO;
import com.example.starkindustry.dao.InvoiceDAO;
import com.example.starkindustry.dao.PartDAO;
import com.example.starkindustry.model.Customer;
import com.example.starkindustry.model.Invoice;
import com.example.starkindustry.model.InvoiceItem;
import com.example.starkindustry.model.Part;
import com.example.starkindustry.model.User;
import com.example.starkindustry.util.ReportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Transaction UI: create a sales invoice (the "till" screen). */
public class InvoicePanel extends JPanel {

    private static final String WALK_IN = "Walk-in Customer";
    private static final double GARAGE_DISCOUNT = 5;   // % automatically given to Garage customers
    private static final double MAX_DISCOUNT = 20;     // % the cashier can never exceed

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PartDAO partDAO = new PartDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final User loggedUser;

    private final List<InvoiceItem> cart = new ArrayList<>();   // same order as the table rows
    private boolean loadingCustomers = false;

    // invoice header
    private final JComboBox<Customer> cmbCustomer = new JComboBox<>();
    private final JComboBox<String> cmbPayment = new JComboBox<>(new String[]{"Cash", "Card", "Credit"});

    // part picker
    private final JTextField txtFind = new JTextField(12);
    private final JComboBox<Part> cmbPart = new JComboBox<>();
    private final JLabel lblPartInfo = new JLabel(" ");
    private final JTextField txtQty = new JTextField("1", 4);

    // invoice lines
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Part No", "Part", "Qty", "Unit Price", "Line Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // totals
    private final JTextField txtDiscountPct = new JTextField("0", 5);
    private final JLabel lblSubTotal = new JLabel("0.00");
    private final JLabel lblDiscount = new JLabel("0.00");
    private final JLabel lblTotal = new JLabel("0.00");

    public InvoicePanel(User loggedUser) {
        this.loggedUser = loggedUser;
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        add(buildTop(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        refresh();
    }

    /** Reloads customers and parts (called every time this screen is shown). */
    public void refresh() {
        loadCustomers();
        loadParts(txtFind.getText().trim());
    }

    // ================= layout =================
    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("New Invoice");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        header.setBorder(BorderFactory.createTitledBorder("Invoice details"));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbCustomer.setPreferredSize(new Dimension(220, 28));
        header.add(new JLabel("Customer *"));
        header.add(cmbCustomer);
        header.add(new JLabel("Payment *"));
        header.add(cmbPayment);
        header.add(new JLabel("Date: " + LocalDate.now()));

        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        picker.setBorder(BorderFactory.createTitledBorder("Add part"));
        picker.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnFind = new JButton("Find");
        JButton btnAdd = new JButton("Add to Invoice");
        cmbPart.setPreferredSize(new Dimension(320, 28));
        picker.add(new JLabel("Find:"));
        picker.add(txtFind);
        picker.add(btnFind);
        picker.add(cmbPart);
        picker.add(new JLabel("Qty"));
        picker.add(txtQty);
        picker.add(btnAdd);

        lblPartInfo.setForeground(Color.GRAY);
        lblPartInfo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        lblPartInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(title);
        top.add(Box.createVerticalStrut(8));
        top.add(header);
        top.add(picker);
        top.add(lblPartInfo);

        // events
        btnFind.addActionListener(e -> loadParts(txtFind.getText().trim()));
        txtFind.addActionListener(e -> loadParts(txtFind.getText().trim()));
        cmbPart.addActionListener(e -> showPartInfo());
        btnAdd.addActionListener(e -> addToInvoice());
        txtQty.addActionListener(e -> addToInvoice());
        cmbCustomer.addActionListener(e -> customerChanged());
        return top;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(table);
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout());

        JButton btnRemove = new JButton("Remove Selected Line");
        btnRemove.addActionListener(e -> removeSelected());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(btnRemove);

        // totals block
        JPanel totals = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 8, 3, 8);
        c.anchor = GridBagConstraints.EAST;

        c.gridy = 0; c.gridx = 0; totals.add(new JLabel("Sub Total (Rs.)"), c);
        c.gridx = 1; totals.add(lblSubTotal, c);

        JPanel pctPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pctPanel.add(new JLabel("Discount %"));
        pctPanel.add(txtDiscountPct);
        c.gridy = 1; c.gridx = 0; totals.add(pctPanel, c);
        c.gridx = 1; totals.add(lblDiscount, c);

        JLabel totalCaption = new JLabel("TOTAL (Rs.)");
        totalCaption.setFont(totalCaption.getFont().deriveFont(Font.BOLD, 16f));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 20f));
        lblTotal.setForeground(new Color(46, 125, 50));
        c.gridy = 2; c.gridx = 0; totals.add(totalCaption, c);
        c.gridx = 1; totals.add(lblTotal, c);

        JButton btnSave = new JButton("Save Invoice");
        JButton btnClear = new JButton("Clear");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.add(btnClear);
        buttons.add(btnSave);
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; totals.add(buttons, c);

        btnSave.addActionListener(e -> saveInvoice());
        btnClear.addActionListener(e -> clearInvoice());
        txtDiscountPct.addActionListener(e -> recalcTotals());
        txtDiscountPct.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                recalcTotals();
            }
        });

        bottom.add(left, BorderLayout.WEST);
        bottom.add(totals, BorderLayout.EAST);
        return bottom;
    }

    // ================= loading data =================
    private void loadCustomers() {
        try {
            Customer previous = (Customer) cmbCustomer.getSelectedItem();
            loadingCustomers = true;
            cmbCustomer.removeAllItems();
            Customer toSelect = null;
            for (Customer c : customerDAO.getAll()) {
                cmbCustomer.addItem(c);
                if (previous != null && c.getCustomerId() == previous.getCustomerId()) {
                    toSelect = c;
                } else if (toSelect == null && previous == null && WALK_IN.equals(c.getName())) {
                    toSelect = c;   // default customer
                }
            }
            if (toSelect != null) {
                cmbCustomer.setSelectedItem(toSelect);
            }
            loadingCustomers = false;
            customerChanged();
        } catch (RuntimeException ex) {
            loadingCustomers = false;
            showError(ex.getMessage());
        }
    }

    private void loadParts(String keyword) {
        try {
            cmbPart.removeAllItems();
            for (Part p : partDAO.search(keyword)) {
                cmbPart.addItem(p);
            }
            showPartInfo();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void showPartInfo() {
        Part p = (Part) cmbPart.getSelectedItem();
        if (p == null) {
            lblPartInfo.setText("No parts found");
            return;
        }
        lblPartInfo.setText(String.format("%s  |  Brand: %s  |  Price: Rs. %,.2f  |  In stock: %d",
                p.getName(), p.getBrand(), p.getUnitPrice(), p.getStockQty()));
    }

    /** Garage customers automatically get the garage discount. */
    private void customerChanged() {
        if (loadingCustomers) {
            return;
        }
        Customer c = (Customer) cmbCustomer.getSelectedItem();
        boolean garage = c != null && "Garage".equals(c.getCustomerType());
        txtDiscountPct.setText(garage ? String.valueOf((int) GARAGE_DISCOUNT) : "0");
        recalcTotals();
    }

    // ================= invoice lines =================
    private void addToInvoice() {
        Part p = (Part) cmbPart.getSelectedItem();
        if (p == null) {
            showWarning("Select a part first.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Quantity must be a whole number.");
            return;
        }
        if (qty <= 0) {
            showWarning("Quantity must be greater than zero.");
            return;
        }
        InvoiceItem existing = findInCart(p.getPartId());
        int alreadyOnInvoice = existing == null ? 0 : existing.getQuantity();
        if (alreadyOnInvoice + qty > p.getStockQty()) {
            showWarning("Not enough stock for " + p.getPartNo() + ".\nIn stock: " + p.getStockQty()
                    + ", already on this invoice: " + alreadyOnInvoice);
            return;
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            existing.setLineTotal(round2(existing.getQuantity() * existing.getUnitPrice()));
        } else {
            InvoiceItem item = new InvoiceItem();
            item.setPartId(p.getPartId());
            item.setPartNo(p.getPartNo());
            item.setPartName(p.getName());
            item.setQuantity(qty);
            item.setUnitPrice(p.getUnitPrice());
            item.setLineTotal(round2(qty * p.getUnitPrice()));
            cart.add(item);
        }
        txtQty.setText("1");
        refreshCartTable();
    }

    private InvoiceItem findInCart(int partId) {
        for (InvoiceItem item : cart) {
            if (item.getPartId() == partId) {
                return item;
            }
        }
        return null;
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Select a line in the table first.");
            return;
        }
        cart.remove(row);
        refreshCartTable();
    }

    private void refreshCartTable() {
        tableModel.setRowCount(0);
        for (InvoiceItem item : cart) {
            tableModel.addRow(new Object[]{
                    item.getPartNo(), item.getPartName(), item.getQuantity(),
                    String.format("%,.2f", item.getUnitPrice()),
                    String.format("%,.2f", item.getLineTotal())
            });
        }
        recalcTotals();
    }

    // ================= totals =================
    private double cartSubTotal() {
        double sum = 0;
        for (InvoiceItem item : cart) {
            sum += item.getLineTotal();
        }
        return round2(sum);
    }

    private void recalcTotals() {
        double subTotal = cartSubTotal();
        double pct = 0;
        try {
            pct = Double.parseDouble(txtDiscountPct.getText().trim());
        } catch (NumberFormatException e) {
            pct = 0;   // invalid values are reported when saving
        }
        double discount = round2(subTotal * pct / 100.0);
        lblSubTotal.setText(String.format("%,.2f", subTotal));
        lblDiscount.setText(String.format("- %,.2f", discount));
        lblTotal.setText(String.format("%,.2f", subTotal - discount));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ================= save =================
    private void saveInvoice() {
        StringBuilder errors = new StringBuilder();

        Customer customer = (Customer) cmbCustomer.getSelectedItem();
        String payment = (String) cmbPayment.getSelectedItem();

        if (customer == null) {
            errors.append("- Select a customer\n");
        }
        if (cart.isEmpty()) {
            errors.append("- Add at least one part to the invoice\n");
        }
        double pct = 0;
        try {
            pct = Double.parseDouble(txtDiscountPct.getText().trim());
            if (pct < 0 || pct > MAX_DISCOUNT) {
                errors.append("- Discount must be between 0 and " + (int) MAX_DISCOUNT + " %\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Discount must be a number\n");
        }
        if (customer != null && WALK_IN.equals(customer.getName()) && "Credit".equals(payment)) {
            errors.append("- Credit sales are not allowed for the Walk-in Customer\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Please fix these", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double subTotal = cartSubTotal();
        double discount = round2(subTotal * pct / 100.0);

        Invoice inv = new Invoice();
        inv.setCustomerId(customer.getCustomerId());
        inv.setUserId(loggedUser.getUserId());
        inv.setInvoiceDate(LocalDate.now());
        inv.setSubTotal(subTotal);
        inv.setDiscount(discount);
        inv.setTotalAmount(round2(subTotal - discount));
        inv.setPaymentMethod(payment);
        inv.setItems(new ArrayList<>(cart));

        try {
            int invoiceId = invoiceDAO.save(inv);
            clearInvoice();
            loadParts(txtFind.getText().trim());   // stock has changed

            int print = JOptionPane.showConfirmDialog(this, String.format(
                            "Invoice #%d saved.%nTotal: Rs. %,.2f%n%nPrint the invoice?", invoiceId, inv.getTotalAmount()),
                    "Saved", JOptionPane.YES_NO_OPTION);
            if (print == JOptionPane.YES_OPTION) {
                ReportUtil.showInvoice(invoiceId);
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearInvoice() {
        cart.clear();
        txtQty.setText("1");
        cmbPayment.setSelectedIndex(0);
        refreshCartTable();
        customerChanged();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Check this", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}