package com.example.starkindustry.dao;

import com.example.starkindustry.config.DBConnection;
import com.example.starkindustry.model.Category;
import com.example.starkindustry.model.Part;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the parts (and categories) tables. */
public class PartDAO {

    private static final String SELECT_PARTS =
            "SELECT p.part_id, p.part_no, p.name, p.category_id, c.name AS category_name, p.brand, " +
                    "p.vehicle_model, p.cost_price, p.unit_price, p.stock_qty, p.reorder_level " +
                    "FROM parts p JOIN categories c ON p.category_id = c.category_id ";

    public List<Part> getAll() {
        return search("");
    }

    /** Finds parts whose part no, name, brand or vehicle model contains the keyword. */
    public List<Part> search(String keyword) {
        String sql = SELECT_PARTS +
                "WHERE p.part_no LIKE ? OR p.name LIKE ? OR p.brand LIKE ? OR p.vehicle_model LIKE ? " +
                "ORDER BY p.part_no";
        List<Part> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String like = "%" + keyword + "%";
                for (int i = 1; i <= 4; i++) {
                    ps.setString(i, like);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapPart(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load parts.", e);
        }
        return list;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT category_id, name FROM categories ORDER BY name")) {
                while (rs.next()) {
                    Category c = new Category();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setName(rs.getString("name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load categories.", e);
        }
        return list;
    }

    public void insert(Part p) {
        String sql = "INSERT INTO parts (part_no, name, category_id, brand, vehicle_model, " +
                "cost_price, unit_price, stock_qty, reorder_level) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                fillStatement(ps, p);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {   // duplicate key
                throw new RuntimeException("Part number '" + p.getPartNo() + "' already exists.", e);
            }
            throw new RuntimeException("Could not save the part.", e);
        }
    }

    public void update(Part p) {
        String sql = "UPDATE parts SET part_no=?, name=?, category_id=?, brand=?, vehicle_model=?, " +
                "cost_price=?, unit_price=?, stock_qty=?, reorder_level=? WHERE part_id=?";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                fillStatement(ps, p);
                ps.setInt(10, p.getPartId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("Part number '" + p.getPartNo() + "' already exists.", e);
            }
            throw new RuntimeException("Could not update the part.", e);
        }
    }

    public void delete(int partId) {
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM parts WHERE part_id = ?")) {
                ps.setInt(1, partId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {   // foreign key: used in invoices
                throw new RuntimeException("This part is used in invoices, so it cannot be deleted.", e);
            }
            throw new RuntimeException("Could not delete the part.", e);
        }
    }

    // ---------- helpers ----------
    private void fillStatement(PreparedStatement ps, Part p) throws SQLException {
        ps.setString(1, p.getPartNo());
        ps.setString(2, p.getName());
        ps.setInt(3, p.getCategoryId());
        ps.setString(4, p.getBrand());
        ps.setString(5, p.getVehicleModel());
        ps.setDouble(6, p.getCostPrice());
        ps.setDouble(7, p.getUnitPrice());
        ps.setInt(8, p.getStockQty());
        ps.setInt(9, p.getReorderLevel());
    }

    private Part mapPart(ResultSet rs) throws SQLException {
        Part p = new Part();
        p.setPartId(rs.getInt("part_id"));
        p.setPartNo(rs.getString("part_no"));
        p.setName(rs.getString("name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setBrand(rs.getString("brand"));
        p.setVehicleModel(rs.getString("vehicle_model"));
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setUnitPrice(rs.getDouble("unit_price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        return p;
    }
}