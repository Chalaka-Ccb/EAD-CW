package com.example.starkindustry.ui;

import com.example.starkindustry.util.ReportUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Reports screen: pick a date range for the big report, or an invoice number for the small one. */
public class ReportsPanel extends JPanel {

    private final JTextField txtFrom = new JTextField(LocalDate.now().minusDays(30).toString(), 10);
    private final JTextField txtTo = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField txtInvoiceNo = new JTextField(8);

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Reports");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel();
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        cards.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        cards.add(buildRestockCard());
        cards.add(Box.createVerticalStrut(20));
        cards.add(buildInvoiceCard());
        add(cards, BorderLayout.CENTER);
    }

    private JPanel buildRestockCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createTitledBorder("Restock and Profit Decision Report"));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel info = new JLabel("<html>Shows every part with the quantity sold, revenue and profit in the chosen period, "
                + "its current stock and whether it must be reordered.<br>Use it to decide what to buy from suppliers.</html>");

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btn = new JButton("Generate Report");
        row.add(new JLabel("From (yyyy-mm-dd)"));
        row.add(txtFrom);
        row.add(new JLabel("To"));
        row.add(txtTo);
        row.add(btn);
        btn.addActionListener(e -> openRestockReport());

        card.add(info, BorderLayout.NORTH);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInvoiceCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createTitledBorder("Print an Invoice"));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel info = new JLabel("Opens a printable copy of any saved invoice.");

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btn = new JButton("Open Invoice");
        row.add(new JLabel("Invoice No"));
        row.add(txtInvoiceNo);
        row.add(btn);
        btn.addActionListener(e -> openInvoice());

        card.add(info, BorderLayout.NORTH);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    // ================= actions =================
    private void openRestockReport() {
        StringBuilder errors = new StringBuilder();
        LocalDate from = null;
        LocalDate to = null;

        try {
            from = LocalDate.parse(txtFrom.getText().trim());
        } catch (DateTimeParseException e) {
            errors.append("- From date must look like 2026-10-01\n");
        }
        try {
            to = LocalDate.parse(txtTo.getText().trim());
        } catch (DateTimeParseException e) {
            errors.append("- To date must look like 2026-10-31\n");
        }
        if (from != null && to != null && from.isAfter(to)) {
            errors.append("- From date cannot be after the To date\n");
        }
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, errors.toString(), "Please fix these", JOptionPane.WARNING_MESSAGE);
            return;
        }

        runReport(() -> ReportUtil.showRestockReport(txtFromDate(), txtToDate()));
    }

    private LocalDate txtFromDate() {
        return LocalDate.parse(txtFrom.getText().trim());
    }

    private LocalDate txtToDate() {
        return LocalDate.parse(txtTo.getText().trim());
    }

    private void openInvoice() {
        int invoiceId;
        try {
            invoiceId = Integer.parseInt(txtInvoiceNo.getText().trim());
            if (invoiceId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invoice No must be a positive whole number.",
                    "Please fix this", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final int id = invoiceId;
        runReport(() -> ReportUtil.showInvoice(id));
    }

    /** Shows a wait cursor while the report is being built and reports any error nicely. */
    private void runReport(Runnable action) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            action.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Report error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}