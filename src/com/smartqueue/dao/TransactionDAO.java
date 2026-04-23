package com.smartqueue.dao;

import com.smartqueue.model.CartItem;
import com.smartqueue.model.Product;
import com.smartqueue.model.User;
import com.smartqueue.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO.java — Handles saving and retrieving checkout transactions.
 */
public class TransactionDAO {

    /**
     * Saves a completed checkout transaction to the database (atomic).
     */
    public int saveTransaction(User user, List<CartItem> cartItems, double totalAmount) {
        String headerSQL = "INSERT INTO transactions (user_id, total_amount, payment_status) VALUES (?,?,?)";
        String itemSQL   = "INSERT INTO transaction_items (transaction_id, product_id, quantity, unit_price) VALUES (?,?,?,?)";

        Connection con = null;
        int generatedId = -1;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false);

            PreparedStatement headerPs = con.prepareStatement(headerSQL, Statement.RETURN_GENERATED_KEYS);
            headerPs.setInt(1, user.getUserId());
            headerPs.setDouble(2, totalAmount);
            headerPs.setString(3, "PENDING");
            headerPs.executeUpdate();

            ResultSet keys = headerPs.getGeneratedKeys();
            if (keys.next()) {
                generatedId = keys.getInt(1);
            }

            PreparedStatement itemPs = con.prepareStatement(itemSQL);
            for (CartItem ci : cartItems) {
                itemPs.setInt(1, generatedId);
                itemPs.setInt(2, ci.getProduct().getProductId());
                itemPs.setInt(3, ci.getQuantity());
                itemPs.setDouble(4, ci.getProduct().getPrice());
                itemPs.addBatch();
            }
            itemPs.executeBatch();
            con.commit();

        } catch (SQLException e) {
            System.err.println("TransactionDAO.saveTransaction error: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            return -1;
        } finally {
            DBUtil.close(con);
        }
        return generatedId;
    }

    /**
     * BUG FIX: Loads CartItems from DB for a given transaction.
     * Used by KioskServlet when the session cart is unavailable
     * (e.g., different browser tab or session was recreated).
     *
     * @param transactionId The transaction whose items to load
     * @return List of CartItem objects populated from DB, or empty list
     */
    public List<CartItem> getCartItems(int transactionId) {
        List<CartItem> items = new ArrayList<CartItem>();
        String sql = "SELECT p.product_id, p.name, p.barcode, p.price, " +
                     "p.expected_weight_grams, p.stock_qty, p.category, p.image_url, " +
                     "ti.quantity " +
                     "FROM transaction_items ti " +
                     "JOIN products p ON ti.product_id = p.product_id " +
                     "WHERE ti.transaction_id = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setBarcode(rs.getString("barcode"));
                p.setPrice(rs.getDouble("price"));
                p.setExpectedWeightGrams(rs.getInt("expected_weight_grams"));
                p.setStockQty(rs.getInt("stock_qty"));
                p.setCategory(rs.getString("category"));
                p.setImageUrl(rs.getString("image_url"));
                int qty = rs.getInt("quantity");
                items.add(new CartItem(p, qty));
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.getCartItems error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return items;
    }

    /**
     * Marks a transaction as PAID after kiosk weight verification passes.
     */
    public boolean markAsPaid(int transactionId) {
        String sql = "UPDATE transactions SET payment_status = 'PAID', paid_at = NOW() WHERE transaction_id = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TransactionDAO.markAsPaid error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return false;
    }

    /**
     * Retrieves the most recent PENDING transaction ID for a given user.
     */
    public int getPendingTransactionId(int userId) {
        String sql = "SELECT transaction_id FROM transactions WHERE user_id = ? " +
                     "AND payment_status = 'PENDING' ORDER BY created_at DESC LIMIT 1";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("transaction_id");
            }
        } catch (SQLException e) {
            System.err.println("TransactionDAO.getPendingTransactionId error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return -1;
    }
}
