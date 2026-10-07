package com.example.starkindustry.dao;

import com.example.starkindustry.config.DBConnection;
import com.example.starkindustry.model.Part;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only queries for the dashboard numbers, chart and low-stock list. */
public class DashboardDAO {

    public double getTodaySales() {
        return queryDouble("SELECT COALESCE(SUM(total_amount), 0) FROM sales_invoices WHERE invoice_date = CURDATE()");
    }

    public double getMonthSales() {
        return queryDouble("SELECT COALESCE(SUM(total_amount), 0) FROM sales_invoices " +
                "WHERE YEAR(invoice_date) = YEAR(CURDATE()) AND MONTH(invoice_date) = MONTH(CURDATE())");
    }

    public int getLowStockCount() {
        return (int) queryDouble("SELECT COUNT(*) FROM parts WHERE stock_qty <= reorder_level");
    }

    /** Real customers only (the Walk-in Customer is not counted). */
    public int getCustomerCount() {
        return (int) queryDouble("SELECT COUNT(*) FROM customers WHERE name <> 'Walk-in Customer'");
    }

    /** Part name -> total units sold, best seller first. */
    public Map<String, Integer> getTopSellingParts(int limit) {
        String sql = "SELECT p.name, SUM(ii.quantity) AS qty FROM invoice_items ii " +
                "JOIN parts p ON ii.part_id = p.part_id " +
                "GROUP BY p.part_id, p.name ORDER BY qty DESC LIMIT ?";
        Map<String, Integer> result = new LinkedHashMap<>();
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.put(rs.getString("name"), rs.getInt("qty"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load top selling parts.", e);
        }
        return result;
    }

    /** Parts at or below their reorder level, the emptiest first. */
    public List<Part> getLowStockParts() {
        String sql = "SELECT part_no, name, stock_qty, reorder_level FROM parts " +
                "WHERE stock_qty <= reorder_level ORDER BY stock_qty - reorder_level";
        List<Part> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Part p = new Part();
                    p.setPartNo(rs.getString("part_no"));
                    p.setName(rs.getString("name"));
                    p.setStockQty(rs.getInt("stock_qty"));
                    p.setReorderLevel(rs.getInt("reorder_level"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load low stock parts.", e);
        }
        return list;
    }

    // ---------- helper for single-number queries ----------
    private double queryDouble(String sql) {
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load dashboard figures.", e);
        }
    }
}