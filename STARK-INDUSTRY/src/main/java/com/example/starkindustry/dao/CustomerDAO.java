package com.example.starkindustry.dao;

import com.example.starkindustry.config.DBConnection;
import com.example.starkindustry.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the customers table. */
public class CustomerDAO {

    public List<Customer> getAll() {
        return search("");
    }

    /** Finds customers whose name, phone or email contains the keyword. */
    public List<Customer> search(String keyword) {
        String sql = "SELECT customer_id, name, phone, email, address, customer_type FROM customers " +
                "WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY name";
        List<Customer> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String like = "%" + keyword + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Customer c = new Customer();
                        c.setCustomerId(rs.getInt("customer_id"));
                        c.setName(rs.getString("name"));
                        c.setPhone(rs.getString("phone"));
                        c.setEmail(rs.getString("email"));
                        c.setAddress(rs.getString("address"));
                        c.setCustomerType(rs.getString("customer_type"));
                        list.add(c);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load customers.", e);
        }
        return list;
    }

    public void insert(Customer c) {
        String sql = "INSERT INTO customers (name, phone, email, address, customer_type) VALUES (?,?,?,?,?)";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getPhone());
                ps.setString(3, c.getEmail());
                ps.setString(4, c.getAddress());
                ps.setString(5, c.getCustomerType());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save the customer.", e);
        }
    }

    public void update(Customer c) {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=?, customer_type=? WHERE customer_id=?";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getPhone());
                ps.setString(3, c.getEmail());
                ps.setString(4, c.getAddress());
                ps.setString(5, c.getCustomerType());
                ps.setInt(6, c.getCustomerId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update the customer.", e);
        }
    }

    public void delete(int customerId) {
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                ps.setInt(1, customerId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {   // foreign key: customer has invoices
                throw new RuntimeException("This customer has invoices, so cannot be deleted.", e);
            }
            throw new RuntimeException("Could not delete the customer.", e);
        }
    }
}