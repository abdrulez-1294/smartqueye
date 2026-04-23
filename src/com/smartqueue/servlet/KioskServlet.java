package com.smartqueue.servlet;

import com.smartqueue.dao.TransactionDAO;
import com.smartqueue.dao.UserDAO;
import com.smartqueue.model.CartItem;
import com.smartqueue.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

/**
 * KioskServlet.java — Backend logic for the Kiosk hardware simulation.
 *
 * URL Mappings:
 *   GET  /kiosk                 → Display kiosk.jsp
 *   POST /kiosk?action=lookup   → Find user by phone, load cart from session OR database
 *   POST /kiosk?action=verify   → Weight verification + mark transaction PAID
 *
 * FIXES APPLIED:
 *   - Replaced switch expressions with if-else (Java 8+ compatible)
 *   - Null-safe weight input parsing (prevents NPE when form field is empty)
 *   - Cart fallback: loads from DB via getCartItems() if not in session
 *     (supports kiosk on separate browser tab from Scan & Go)
 */
@WebServlet(name = "KioskServlet", urlPatterns = {"/kiosk"})
public class KioskServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int  WEIGHT_TOLERANCE_GRAMS = 50;

    private final UserDAO        userDAO        = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // -------------------------------------------------------
    // GET — Show kiosk UI (no session guard: public terminal)
    // -------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // POST — Route to correct action handler
    // -------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        // FIX 1: Use if-else instead of switch expression (Java 8+ compatible)
        if ("lookup".equals(action)) {
            handlePhoneLookup(req, resp);
        } else if ("verify".equals(action)) {
            handleWeightVerification(req, resp);
        } else {
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
        }
    }

    // -------------------------------------------------------
    // Action: LOOKUP — Find user by phone, load cart
    // -------------------------------------------------------
    private void handlePhoneLookup(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String phone = req.getParameter("phone");

        if (phone == null || phone.trim().isEmpty()) {
            req.setAttribute("kioskError", "Please enter your registered phone number.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.findByPhone(phone.trim());

        if (user == null) {
            req.setAttribute("kioskError", "No account found for phone: " + phone.trim());
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("kioskUser", user);

        int txId = transactionDAO.getPendingTransactionId(user.getUserId());

        if (txId == -1) {
            req.setAttribute("kioskError", "No pending transaction for " + user.getFullName()
                    + ". Please checkout via the Scan & Go app first.");
            req.setAttribute("kioskUser", user);
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        session.setAttribute("kioskTransactionId", txId);

        // FIX 2: Try session cart first; if missing, load from database
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            // Cart not in session (e.g., different browser tab) — load from DB
            cart = transactionDAO.getCartItems(txId);
            if (cart != null && !cart.isEmpty()) {
                session.setAttribute("cart", cart); // sync back into session
            }
        }

        req.setAttribute("kioskUser", user);
        req.setAttribute("kioskCart", cart);
        req.setAttribute("kioskTxId",  txId);
        req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // Action: VERIFY — Weight check + payment confirmation
    // -------------------------------------------------------
    private void handleWeightVerification(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("kioskTransactionId") == null) {
            req.setAttribute("kioskError", "Session expired. Please re-enter your phone number.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // FIX 3: Null-safe weight parsing (prevents NPE if field is submitted empty)
        String weightStr = req.getParameter("actualWeightGrams");
        if (weightStr == null || weightStr.trim().isEmpty()) {
            req.setAttribute("kioskError", "Please enter the weight shown on the scale.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        int actualWeight;
        try {
            actualWeight = Integer.parseInt(weightStr.trim());
        } catch (NumberFormatException e) {
            req.setAttribute("kioskError", "Invalid weight. Please enter a number in grams.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // FIX 4: Fallback to DB if session cart is gone
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            int txId = (int) session.getAttribute("kioskTransactionId");
            cart = transactionDAO.getCartItems(txId);
        }

        if (cart == null || cart.isEmpty()) {
            req.setAttribute("kioskError", "Cart data unavailable. Cannot verify weight. Contact staff.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // Calculate expected weight from all cart items
        int expectedWeight = 0;
        for (CartItem item : cart) {
            expectedWeight += item.getLineTotalWeightGrams();
        }

        // -------------------------------------------------------
        // CORE SSP WEIGHT VERIFICATION LOGIC
        // -------------------------------------------------------
        int     difference     = Math.abs(actualWeight - expectedWeight);
        boolean weightVerified = (difference <= WEIGHT_TOLERANCE_GRAMS);

        req.setAttribute("expectedWeight", expectedWeight);
        req.setAttribute("actualWeight",   actualWeight);
        req.setAttribute("weightDiff",     difference);
        req.setAttribute("kioskCart",      cart);
        req.setAttribute("kioskUser",      session.getAttribute("kioskUser"));

        if (weightVerified) {
            int txId   = (int) session.getAttribute("kioskTransactionId");
            boolean paid = transactionDAO.markAsPaid(txId);

            if (paid) {
                // Clear all session keys related to this transaction
                session.removeAttribute("cart");
                session.removeAttribute("lastTransactionId");
                session.removeAttribute("checkoutTotal");
                session.removeAttribute("kioskTransactionId");
                session.removeAttribute("kioskUser");

                req.setAttribute("verificationSuccess", true);
                req.setAttribute("transactionId", txId);
                req.setAttribute("kioskMsg", "Weight Verified! Payment confirmed. You may exit.");
            } else {
                req.setAttribute("kioskError", "Payment confirmation failed. Please contact staff.");
            }
        } else {
            String direction = (actualWeight > expectedWeight) ? "heavier" : "lighter";
            req.setAttribute("kioskError",
                "Weight mismatch! Expected: " + expectedWeight + "g | Actual: " + actualWeight
                + "g | Difference: " + difference + "g "
                + "(Bag appears " + direction + " than expected). "
                + "Please remove unscanned items or contact staff.");
            // Re-pass the cart for display
            req.setAttribute("kioskTxId", session.getAttribute("kioskTransactionId"));
        }

        req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
    }
}
