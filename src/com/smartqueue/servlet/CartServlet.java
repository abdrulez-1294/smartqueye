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
 * CartServlet.java — The core shopping cart controller for the "Scan & Go" interface.
 *
 * URL Mappings:
 *   POST /cart?action=scan       → Scan a barcode and add product to session cart
 *   POST /cart?action=remove     → Remove an item from the session cart
 *   POST /cart?action=checkout   → Save cart to DB and generate invoice/QR
 *   POST /cart?action=clear      → Clear all items from the session cart
 *
 * Session Key: "cart" — stores a List<CartItem>
 *
 * MVC Role: Controller
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

        // -------------------------------------------------------
        // Session Guard — redirect to login if not authenticated
        // -------------------------------------------------------
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "scan"     -> handleScan(req, resp, session);
            case "remove"   -> handleRemove(req, resp, session);
            case "checkout" -> handleCheckout(req, resp, session);
            case "clear"    -> handleClear(req, resp, session);
            default         -> resp.sendRedirect(req.getContextPath() + "/shop.jsp");
        }
    }

    // -------------------------------------------------------
    // Action: SCAN — Look up product by barcode, add to cart
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleScan(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException, ServletException {

        String barcode = req.getParameter("barcode");

        if (barcode == null || barcode.isBlank()) {
            req.setAttribute("scanError", "Please enter a valid barcode.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        // Look up product in database
        Product product = productDAO.findByBarcode(barcode.trim());

        if (product == null) {
            req.setAttribute("scanError", "Product not found for barcode: " + barcode);
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        // Check stock availability
        if (product.getStockQty() <= 0) {
            req.setAttribute("scanError", "'" + product.getName() + "' is currently out of stock.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        // Retrieve or create the cart from the session
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Check if item already exists in cart — if so, increment quantity
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getBarcode().equals(product.getBarcode())) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            // New item — add with quantity 1
            cart.add(new CartItem(product, 1));
        }

        // Save the updated cart back to session
        session.setAttribute("cart", cart);

        // Forward to shop with success message
        req.setAttribute("scanSuccess", "'" + product.getName() + "' added to cart!");
        req.getRequestDispatcher("/shop.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // Action: REMOVE — Remove a specific item from the cart
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleRemove(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException {

        String barcodeToRemove = req.getParameter("barcode");
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart != null && barcodeToRemove != null) {
            cart.removeIf(item -> item.getProduct().getBarcode().equals(barcodeToRemove));
            session.setAttribute("cart", cart);
        }

        resp.sendRedirect(req.getContextPath() + "/shop.jsp");
    }

    // -------------------------------------------------------
    // Action: CLEAR — Empty the entire cart
    // -------------------------------------------------------
    private void handleClear(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException {

        session.removeAttribute("cart");
        session.removeAttribute("lastTransactionId");
        resp.sendRedirect(req.getContextPath() + "/shop.jsp");
    }

    // -------------------------------------------------------
    // Action: CHECKOUT — Save transaction to DB, generate invoice
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleCheckout(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException, ServletException {

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        User user           = (User) session.getAttribute("loggedInUser");

        if (cart == null || cart.isEmpty()) {
            req.setAttribute("scanError", "Your cart is empty. Please scan items first.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        // Calculate grand total
        double total = 0;
        for (CartItem item : cart) {
            total += item.getLineTotal();
        }

        // Persist transaction to database
        int transactionId = transactionDAO.saveTransaction(user, cart, total);

        if (transactionId == -1) {
            req.setAttribute("scanError", "Checkout failed. Please try again.");
            req.getRequestDispatcher("/shop.jsp").forward(req, resp);
            return;
        }

        // Store transaction ID in session for kiosk verification
        session.setAttribute("lastTransactionId", transactionId);
        session.setAttribute("checkoutTotal", total);

        // Redirect to the invoice/receipt page
        resp.sendRedirect(req.getContextPath() + "/invoice.jsp");
    }
}
