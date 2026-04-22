package com.smartqueue.dao;

import com.smartqueue.model.Product;
import com.smartqueue.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO.java — Data Access Object for Product-related database operations.
 *
 * Handles CRUD operations on the 'products' table.
 * Called by AdminServlet and CartServlet.
 */
public class ProductDAO {

    /**
     * Fetches all active products from the database.
     *
     * @return List of all Product objects
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY category, name";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("ProductDAO.getAllProducts error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return list;
    }

    /**
     * Finds a product by its barcode string.
     * Used by the Scan & Go interface when a barcode is submitted.
     *
     * @param barcode The barcode string to search for
     * @return Product object if found, null otherwise
     */
    public Product findByBarcode(String barcode) {
        String sql = "SELECT * FROM products WHERE barcode = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, barcode.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("ProductDAO.findByBarcode error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return null;
    }

    /**
     * Finds a product by its primary key ID.
     *
     * @param productId The product's database ID
     * @return Product object if found, null otherwise
     */
    public Product findById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("ProductDAO.findById error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return null;
    }

    /**
     * Inserts a new product into the database (Admin use).
     *
     * @param p Product object to insert
     * @return true if successful
     */
    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (name, barcode, price, expected_weight_grams, stock_qty, category, image_url) VALUES (?,?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setString(2, p.getBarcode());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getExpectedWeightGrams());
            ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getImageUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductDAO.addProduct error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return false;
    }

    /**
     * Updates an existing product's details (Admin use).
     *
     * @param p Product object with updated values (must have valid productId)
     * @return true if successful
     */
    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET name=?, barcode=?, price=?, expected_weight_grams=?, stock_qty=?, category=?, image_url=? WHERE product_id=?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setString(2, p.getBarcode());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getExpectedWeightGrams());
            ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getImageUrl());
            ps.setInt(8, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductDAO.updateProduct error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return false;
    }

    /**
     * Deletes a product by ID (Admin use).
     *
     * @param productId The product to delete
     * @return true if successful
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        Connection con = null;
        try {
            con = DBUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ProductDAO.deleteProduct error: " + e.getMessage());
        } finally {
            DBUtil.close(con);
        }
        return false;
    }

    // -------------------------------------------------------
    // Private Helper
    // -------------------------------------------------------

    /** Maps a ResultSet row to a Product object. */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setBarcode(rs.getString("barcode"));
        p.setPrice(rs.getDouble("price"));
        p.setExpectedWeightGrams(rs.getInt("expected_weight_grams"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        return p;
    }
}
