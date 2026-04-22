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
 *   POST /kiosk?action=lookup   → Finds user by phone, syncs cart from DB
 *   POST /kiosk?action=verify   → Weight verification logic + payment confirmation
 *   GET  /kiosk                 → Displays the kiosk.jsp interface
 *
 * Key Feature: Weight Verification
 *   The servlet computes the expected total weight from all cart items
 *   and compares it to the weight entered by the user at the kiosk (simulated input).
 *   A tolerance of ±50g is allowed to account for packaging variance.
 *
 * MVC Role: Controller
 */
@WebServlet(name = "KioskServlet", urlPatterns = {"/kiosk"})
public class KioskServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Tolerance (in grams) allowed between expected and actual weight
    private static final int WEIGHT_TOLERANCE_GRAMS = 50;

    private final UserDAO        userDAO        = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // -------------------------------------------------------
    // GET — Display Kiosk UI
    // -------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Simply forward to the kiosk JSP view
        req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // POST — Handle kiosk actions
    // -------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "lookup" -> handlePhoneLookup(req, resp);
            case "verify" -> handleWeightVerification(req, resp);
            default       -> req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
        }
    }

    // -------------------------------------------------------
    // Action: LOOKUP — Find user by phone number
    // -------------------------------------------------------
    private void handlePhoneLookup(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String phone = req.getParameter("phone");

        if (phone == null || phone.isBlank()) {
            req.setAttribute("kioskError", "Please enter your registered phone number.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // Look up user by phone (kiosk identifier)
        User user = userDAO.findByPhone(phone.trim());

        if (user == null) {
            req.setAttribute("kioskError", "No account found for phone number: " + phone);
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // Use the session from the Scan & Go app if available,
        // otherwise create a new kiosk-specific session
        HttpSession session = req.getSession(true);
        session.setAttribute("kioskUser", user);

        // Retrieve the PENDING transaction ID for this user
        int txId = transactionDAO.getPendingTransactionId(user.getUserId());

        if (txId == -1) {
            req.setAttribute("kioskError", "No pending transaction found for " + user.getFullName()
                    + ". Please complete checkout in the Scan & Go app first.");
            req.setAttribute("kioskUser", user);
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // Check if the shared session contains the cart from the Scan & Go app
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        session.setAttribute("kioskTransactionId", txId);
        req.setAttribute("kioskUser", user);
        req.setAttribute("kioskCart", cart);
        req.setAttribute("kioskTxId", txId);
        req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
    }

    // -------------------------------------------------------
    // Action: VERIFY — Weight verification + payment confirmation
    // -------------------------------------------------------
    private void handleWeightVerification(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession session = req.getSession(false);

        // Guard: must have a kiosk session with transaction
        if (session == null || session.getAttribute("kioskTransactionId") == null) {
            req.setAttribute("kioskError", "Session expired. Please re-enter your phone number.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // Parse the weight entered by the customer at the kiosk scale (simulated input)
        String weightStr = req.getParameter("actualWeightGrams");
        int actualWeight;
        try {
            actualWeight = Integer.parseInt(weightStr.trim());
        } catch (NumberFormatException e) {
            req.setAttribute("kioskError", "Invalid weight entered. Please enter a numeric value in grams.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        // Calculate expected total weight from all cart items
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            req.setAttribute("kioskError", "Cart data is unavailable. Cannot verify weight.");
            req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
            return;
        }

        int expectedWeight = 0;
        for (CartItem item : cart) {
            expectedWeight += item.getLineTotalWeightGrams();
        }

        // -------------------------------------------------------
        // CORE WEIGHT VERIFICATION LOGIC
        // Compare actual vs expected weight within tolerance band
        // -------------------------------------------------------
        int difference = Math.abs(actualWeight - expectedWeight);
        boolean weightVerified = (difference <= WEIGHT_TOLERANCE_GRAMS);

        req.setAttribute("expectedWeight", expectedWeight);
        req.setAttribute("actualWeight",   actualWeight);
        req.setAttribute("weightDiff",     difference);
        req.setAttribute("kioskCart",      cart);
        req.setAttribute("kioskUser",      session.getAttribute("kioskUser"));

        if (weightVerified) {
            // Weight matches — proceed to mark transaction as PAID
            int txId = (int) session.getAttribute("kioskTransactionId");
            boolean paid = transactionDAO.markAsPaid(txId);

            if (paid) {
                // Clear cart from session after successful payment
                session.removeAttribute("cart");
                session.removeAttribute("lastTransactionId");
                session.removeAttribute("checkoutTotal");
                session.removeAttribute("kioskTransactionId");

                req.setAttribute("verificationSuccess", true);
                req.setAttribute("transactionId", txId);
                req.setAttribute("kioskMsg", "✅ Weight Verified! Payment confirmed. You may exit.");
            } else {
                req.setAttribute("kioskError", "Payment confirmation failed. Please contact staff.");
            }
        } else {
            // Weight mismatch — potential theft/error alert
            String direction = (actualWeight > expectedWeight) ? "heavier" : "lighter";
            req.setAttribute("kioskError",
                "⚠️ Weight mismatch! Expected: " + expectedWeight + "g | Actual: " + actualWeight + "g | "
                + "Difference: " + difference + "g (Items appear " + direction + " than expected). "
                + "Please remove unscanned items or contact staff.");
        }

        req.getRequestDispatcher("/kiosk.jsp").forward(req, resp);
    }
}
