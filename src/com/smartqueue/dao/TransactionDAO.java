package com.smartqueue.dao;

import com.smartqueue.model.CartItem;
import com.smartqueue.model.User;
import com.smartqueue.util.DBUtil;

import java.sql.*;
import java.util.List;

/**
 * TransactionDAO.java — Handles saving and retrieving checkout transactions.
 *
 * When a customer finalises checkout, a Transaction header record is inserted
 * along with individual Transaction_Items rows (one per cart item).
 */
public class TransactionDAO {

    /**
     * Saves a completed checkout transaction to the database.
     *
     * Inserts one row into 'transactions' (header) and multiple rows into
     * 'transaction_items' (line items). Uses a single DB connection with
     * manual transaction commit for atomicity.
     *
     * @param user       The logged-in customer
     * @param cartItems  List of items in the session cart
     * @param totalAmount The grand total price
     * @return The generated transaction ID, or -1 on failure
     */
    public int saveTransaction(User user, List<CartItem> cartItems, double totalAmount) {
        String headerSQL = "INSERT INTO transactions (user_id, total_amount, payment_status) VALUES (?,?,?)";
        String itemSQL   = "INSERT INTO transaction_items (transaction_id, product_id, quantity, unit_price) VALUES (?,?,?,?)";

        Connection con = null;
        int generatedId = -1;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false); // Begin manual transaction

            // 1. Insert header row
            PreparedStatement headerPs = con.prepareStatement(headerSQL, Statement.RETURN_GENERATED_KEYS);
            headerPs.setInt(1, user.getUserId());
            headerPs.setDouble(2, totalAmount);
            headerPs.setString(3, "PENDING"); // Will be updated to PAID at kiosk verification
            headerPs.executeUpdate();

            ResultSet keys = headerPs.getGeneratedKeys();
            if (keys.next()) {
                generatedId = keys.getInt(1);
            }

            // 2. Insert one line item per cart entry
            PreparedStatement itemPs = con.prepareStatement(itemSQL);
            for (CartItem ci : cartItems) {
                itemPs.setInt(1, generatedId);
                itemPs.setInt(2, ci.getProduct().getProductId());
                itemPs.setInt(3, ci.getQuantity());
                itemPs.setDouble(4, ci.getProduct().getPrice());
                itemPs.addBatch(); // Batch for efficiency
            }
            itemPs.executeBatch();

            con.commit(); // Commit the transaction atomically
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
     * Marks a transaction's payment status as 'PAID' after kiosk verification passes.
     *
     * @param transactionId The transaction to mark as paid
     * @return true if successful
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
     * Retrieves the most recent PENDING transaction for a given user.
     * Used by the Kiosk to load the synced cart.
     *
     * @param userId The customer's user ID
     * @return transaction_id or -1 if none found
     */
    public int getPendingTransactionId(int userId) {
        String sql = "SELECT transaction_id FROM transactions WHERE user_id = ? AND payment_status = 'PENDING' ORDER BY created_at DESC LIMIT 1";
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
