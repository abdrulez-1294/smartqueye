package com.smartqueue.servlet;

import com.smartqueue.dao.ProductDAO;
import com.smartqueue.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * AdminServlet.java — Product inventory management for admin role.
 *
 * URL: GET /admin  →  List products
 *      POST /admin?action=add|update|delete
 *
 * FIX: Replaced switch expression with if-else for Java 8+ compatibility
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    // Role guard — redirects non-admins to login
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

    // GET — load product list and display admin panel
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    // POST — route to CRUD handler
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        // FIX: if-else instead of switch expression
        if ("add".equals(action)) {
            handleAdd(req, resp);
        } else if ("update".equals(action)) {
            handleUpdate(req, resp);
        } else if ("delete".equals(action)) {
            handleDelete(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin");
        }
    }

    private void handleAdd(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        Product p = buildProductFromRequest(req);
        boolean success = productDAO.addProduct(p);
        if (success) {
            req.setAttribute("adminSuccess", "Product '" + p.getName() + "' added successfully.");
        } else {
            req.setAttribute("adminError", "Failed to add product. Barcode may already exist.");
        }
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

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

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        int productId = parseIntSafe(req.getParameter("productId"), 0);
        boolean success = productDAO.deleteProduct(productId);
        if (success) {
            req.setAttribute("adminSuccess", "Product deleted successfully.");
        } else {
            req.setAttribute("adminError", "Failed to delete product.");
        }
        req.setAttribute("productList", productDAO.getAllProducts());
        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    private Product buildProductFromRequest(HttpServletRequest req) {
        Product p = new Product();
        p.setName(req.getParameter("name"));
        p.setBarcode(req.getParameter("barcode"));
        p.setPrice(parseDoubleSafe(req.getParameter("price"), 0.0));
        p.setExpectedWeightGrams(parseIntSafe(req.getParameter("expectedWeightGrams"), 0));
        p.setStockQty(parseIntSafe(req.getParameter("stockQty"), 0));
        p.setCategory(req.getParameter("category"));
        String img = req.getParameter("imageUrl");
        p.setImageUrl(img != null ? img : "");
        return p;
    }

    private int parseIntSafe(String val, int def) {
        try { return Integer.parseInt(val); } catch (Exception e) { return def; }
    }

    private double parseDoubleSafe(String val, double def) {
        try { return Double.parseDouble(val); } catch (Exception e) { return def; }
    }
}
