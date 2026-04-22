package com.smartqueue.servlet;

import com.smartqueue.dao.ProductDAO;
import com.smartqueue.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * AdminServlet.java — Handles product inventory management for admin users.
 *
 * URL Mappings (all under /admin):
 *   GET  /admin?action=list    → Show all products in admin panel
 *   POST /admin?action=add     → Add a new product
 *   POST /admin?action=update  → Update an existing product
 *   POST /admin?action=delete  → Delete a product
 *
 * Access Control: Only users with role="admin" can access this servlet.
 *
 * MVC Role: Controller
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    // -------------------------------------------------------
    // Session/Role Guard — used in both GET and POST
    // -------------------------------------------------------
    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null
                || !"admin".equals(session.getAttribute("role"))) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return false;
        }
        return true;
    }

    // -------------------------------------------------------
    // GET — Load product list for admin.jsp
    // -------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        // Load all products and pass to JSP
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // POST — Handle product add/update/delete
    // -------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "add"    -> handleAdd(req, resp);
            case "update" -> handleUpdate(req, resp);
            case "delete" -> handleDelete(req, resp);
            default       -> resp.sendRedirect(req.getContextPath() + "/admin");
        }
    }

    // -------------------------------------------------------
    // Private: Add product
    // -------------------------------------------------------
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        Product p = buildProductFromRequest(req);
        boolean success = productDAO.addProduct(p);

        if (success) {
            req.setAttribute("adminSuccess", "Product '" + p.getName() + "' added successfully.");
        } else {
            req.setAttribute("adminError", "Failed to add product. Barcode may already exist.");
        }
        // Reload the admin page with updated list
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // Private: Update product
    // -------------------------------------------------------
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        Product p = buildProductFromRequest(req);
        p.setProductId(parseIntSafe(req.getParameter("productId"), 0));
        boolean success = productDAO.updateProduct(p);

        if (success) {
            req.setAttribute("adminSuccess", "Product updated successfully.");
        } else {
            req.setAttribute("adminError", "Failed to update product.");
        }
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // Private: Delete product
    // -------------------------------------------------------
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        int productId = parseIntSafe(req.getParameter("productId"), 0);
        boolean success = productDAO.deleteProduct(productId);

        if (success) {
            req.setAttribute("adminSuccess", "Product deleted.");
        } else {
            req.setAttribute("adminError", "Failed to delete product.");
        }
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------

    /** Builds a Product object from HTTP request parameters. */
    private Product buildProductFromRequest(HttpServletRequest req) {
        Product p = new Product();
        p.setName(req.getParameter("name"));
        p.setBarcode(req.getParameter("barcode"));
        p.setPrice(parseDoubleSafe(req.getParameter("price"), 0.0));
        p.setExpectedWeightGrams(parseIntSafe(req.getParameter("expectedWeightGrams"), 0));
        p.setStockQty(parseIntSafe(req.getParameter("stockQty"), 0));
        p.setCategory(req.getParameter("category"));
        p.setImageUrl(req.getParameter("imageUrl") != null ? req.getParameter("imageUrl") : "");
        return p;
    }

    private int parseIntSafe(String val, int defaultVal) {
        try { return Integer.parseInt(val); } catch (Exception e) { return defaultVal; }
    }

    private double parseDoubleSafe(String val, double defaultVal) {
        try { return Double.parseDouble(val); } catch (Exception e) { return defaultVal; }
    }
}
