package com.example.starkindustry.dao;

import com.example.starkindustry.config.DBConnection;
import com.example.starkindustry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** DAO (Data Access Object): all SQL for the users table lives here. */
public class UserDAO {

    /** Returns the User if username/password are correct, otherwise null. */
    public User login(String username, String password) {
        String sql = "SELECT user_id, username, full_name FROM users WHERE username = ? AND password = ?";
        try {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setUserId(rs.getInt("user_id"));
                        u.setUsername(rs.getString("username"));
                        u.setFullName(rs.getString("full_name"));
                        return u;
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed due to a database error.", e);
        }
    }
}