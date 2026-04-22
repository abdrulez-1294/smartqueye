package com.smartqueue.dao;

import com.smartqueue.model.User;
import com.smartqueue.util.DBUtil;

import java.sql.*;

/**
 * UserDAO.java — Data Access Object for User-related database operations.
 *
 * Handles all SQL queries related to the 'users' table.
 * Keeps database logic separate from Servlet (Controller) logic — MVC pattern.
 */
public class UserDAO {

    /**
     * Authenticates a user by email and password.
     *
     * @param email    The email submitted from the login form
     * @param password The password submitted from the login form
     * @return User object if credentials match, null otherwise
     */
    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password); // Plain text for academic scope
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.authenticate error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return null;
    }

    /**
     * Finds a user by their phone number (used for Kiosk login).
     *
     * @param phone The phone number entered at the kiosk
     * @return User object if found, null otherwise
     */
    public User findByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.findByPhone error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return null;
    }

    /**
     * Registers a new customer account.
     *
     * @param user The User object containing registration details
     * @return true if inserted successfully, false otherwise
     */
    public boolean register(User user) {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, 'customer')";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPasswordHash());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.register error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return false;
    }

    // -------------------------------------------------------
    // Private Helper
    // -------------------------------------------------------

    /** Maps a ResultSet row to a User object. */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        return u;
    }
}
