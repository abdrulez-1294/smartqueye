package com.smartqueue.servlet;

import com.smartqueue.dao.ProductDAO;
import com.smartqueue.dao.TransactionDAO;
import com.smartqueue.model.CartItem;
import com.smartqueue.model.Product;
import com.smartqueue.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CartServlet.java — Core shopping cart controller for the Scan & Go interface.
 *
 * URL: POST /cart?action=scan|remove|checkout|clear
 * Session key: "cart" stores List<CartItem>
 *
 * FIX: Replaced switch expressions with if-else (Java 8+ compatible)
 * FIX: handleClear now removes checkoutTotal from session
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ProductDAO     productDAO     = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Session guard
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "";

        // FIX: if-else instead of switch expression for Java 8+ compatibility
        if ("scan".equals(action)) {
            handleScan(req, resp, session);
        } else if ("remove".equals(action)) {
            handleRemove(req, resp, session);
        } else if ("checkout".equals(action)) {
            handleCheckout(req, resp, session);
        } else if ("clear".equals(action)) {
            handleClear(req, resp, session);
        } else {
            resp.sendRedirect(req.getContextPath() + "/shop.jsp");
        }
    }

    // -------------------------------------------------------
    // SCAN: Look up product by barcode, add to session cart
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleScan(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException, ServletException {

        String barcode = req.getParameter("barcode");

        if (barcode == null || barcode.trim().isEmpty()) {
            req.setAttribute("scanError", "Please enter a valid barcode.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        Product product = productDAO.findByBarcode(barcode.trim());

        if (product == null) {
            req.setAttribute("scanError", "No product found for barcode: " + barcode.trim());
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        if (product.getStockQty() <= 0) {
            req.setAttribute("scanError", "'" + product.getName() + "' is out of stock.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<CartItem>();
        }

        // Increment quantity if already in cart, else add new entry
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getBarcode().equals(product.getBarcode())) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(product, 1));
        }

        session.setAttribute("cart", cart);
        req.setAttribute("scanSuccess", "'" + product.getName() + "' added to cart!");
        req.getRequestDispatcher("/shop.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // REMOVE: Remove one item from cart by barcode
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleRemove(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException {

        String barcodeToRemove = req.getParameter("barcode");
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart != null && barcodeToRemove != null) {
            List<CartItem> updated = new ArrayList<CartItem>();
            for (CartItem item : cart) {
                if (!item.getProduct().getBarcode().equals(barcodeToRemove)) {
                    updated.add(item);
                }
            }
            session.setAttribute("cart", updated);
        }

        resp.sendRedirect(req.getContextPath() + "/shop.jsp");
    }

    // -------------------------------------------------------
    // CLEAR: Empty entire cart and reset session checkout keys
    // -------------------------------------------------------
    private void handleClear(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException {

        session.removeAttribute("cart");
        session.removeAttribute("lastTransactionId");
        session.removeAttribute("checkoutTotal"); // FIX: was missing before
        resp.sendRedirect(req.getContextPath() + "/shop.jsp");
    }

    // -------------------------------------------------------
    // CHECKOUT: Persist cart to DB, redirect to invoice
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleCheckout(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException, ServletException {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        User user           = (User) session.getAttribute("loggedInUser");

        if (cart == null || cart.isEmpty()) {
            req.setAttribute("scanError", "Your cart is empty. Scan items first.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        double total = 0;
        for (CartItem item : cart) {
            total += item.getLineTotal();
        }

        int transactionId = transactionDAO.saveTransaction(user, cart, total);

        if (transactionId == -1) {
            req.setAttribute("scanError", "Checkout failed. Please check DB connection and try again.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        session.setAttribute("lastTransactionId", transactionId);
        session.setAttribute("checkoutTotal", total);

        resp.sendRedirect(req.getContextPath() + "/invoice.jsp");
    }
}
