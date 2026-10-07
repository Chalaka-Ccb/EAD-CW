package com.example.starkindustry.dao;

import com.example.starkindustry.config.DBConnection;
import com.example.starkindustry.model.Invoice;
import com.example.starkindustry.model.InvoiceItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** All SQL for saving invoices. */
public class InvoiceDAO {

    /**
     * Saves the invoice header, its items and reduces the stock in ONE database transaction.
     * If anything fails, everything is rolled back, so the database never ends up half-saved.
     * Returns the new invoice number.
     */
    public int save(Invoice inv) {
        String headerSql = "INSERT INTO sales_invoices (customer_id, user_id, invoice_date, sub_total, " +
                "discount, total_amount, payment_method) VALUES (?,?,?,?,?,?,?)";
        String itemSql = "INSERT INTO invoice_items (invoice_id, part_id, quantity, unit_price, line_total) " +
                "VALUES (?,?,?,?,?)";
        // the "AND stock_qty >= ?" makes sure stock can never go negative
        String stockSql = "UPDATE parts SET stock_qty = stock_qty - ? WHERE part_id = ? AND stock_qty >= ?";

        Connection con = DBConnection.getInstance().getConnection();
        try {
            con.setAutoCommit(false);   // start the transaction

            int invoiceId;
            try (PreparedStatement ps = con.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, inv.getCustomerId());
                ps.setInt(2, inv.getUserId());
                ps.setDate(3, java.sql.Date.valueOf(inv.getInvoiceDate()));
                ps.setDouble(4, inv.getSubTotal());
                ps.setDouble(5, inv.getDiscount());
                ps.setDouble(6, inv.getTotalAmount());
                ps.setString(7, inv.getPaymentMethod());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    invoiceId = keys.getInt(1);
                }
            }

            try (PreparedStatement psItem = con.prepareStatement(itemSql);
                 PreparedStatement psStock = con.prepareStatement(stockSql)) {
                for (InvoiceItem item : inv.getItems()) {
                    psStock.setInt(1, item.getQuantity());
                    psStock.setInt(2, item.getPartId());
                    psStock.setInt(3, item.getQuantity());
                    if (psStock.executeUpdate() == 0) {
                        throw new IllegalStateException("Not enough stock for part " + item.getPartNo()
                                + ". Nothing was saved.");
                    }
                    psItem.setInt(1, invoiceId);
                    psItem.setInt(2, item.getPartId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getUnitPrice());
                    psItem.setDouble(5, item.getLineTotal());
                    psItem.executeUpdate();
                }
            }

            con.commit();   // everything worked: make it permanent
            return invoiceId;

        } catch (SQLException e) {
            rollback(con);
            throw new RuntimeException("Could not save the invoice. Nothing was saved.", e);
        } catch (RuntimeException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }

    private void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignored) {
            // nothing more we can do
        }
    }
}