package com.smartqueue.servlet;

import com.smartqueue.dao.UserDAO;
import com.smartqueue.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * AuthServlet.java — Handles Login, Registration, and Logout actions.
 *
 * URL Mappings:
 *   POST /login    → doPost authenticates the user and creates a session
 *   GET  /logout   → Invalidates the session and redirects to login page
 *   POST /register → Registers a new customer account
 *
 * MVC Role: Controller
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout", "/register"})
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    // -------------------------------------------------------
    // GET — Handle logout
    // -------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            // Invalidate the HttpSession to clear all stored session data (cart, user info)
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        }
    }

    // -------------------------------------------------------
    // POST — Handle login & registration
    // -------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(req, resp);
        }
    }

    // -------------------------------------------------------
    // Private: Login logic
    // -------------------------------------------------------
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // Validate required fields
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("errorMsg", "Email and password are required.");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            return;
        }

        // Authenticate via DAO
        User user = userDAO.authenticate(email.trim(), password.trim());

        if (user != null) {
            // Create a new session (invalidate old session to prevent fixation attacks)
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute("loggedInUser", user);       // Store user in session
            session.setAttribute("role", user.getRole());     // Store role separately for quick checks
            session.setMaxInactiveInterval(30 * 60);          // 30 minute timeout

            // Redirect based on role
            if ("admin".equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/admin.jsp");
            } else {
                resp.sendRedirect(req.getContextPath() + "/shop.jsp");
            }
        } else {
            // Auth failed — forward back to login with error message
            req.setAttribute("errorMsg", "Invalid email or password. Please try again.");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }

    // -------------------------------------------------------
    // Private: Registration logic
    // -------------------------------------------------------
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String fullName = req.getParameter("fullName");
        String email    = req.getParameter("email");
        String phone    = req.getParameter("phone");
        String password = req.getParameter("password");

        if (fullName == null || email == null || phone == null || password == null
                || fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            req.setAttribute("errorMsg", "All registration fields are required.");
            req.setAttribute("showRegister", true);
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            return;
        }

        User newUser = new User();
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim());
        newUser.setPhone(phone.trim());
        newUser.setPasswordHash(password.trim()); // Plain text (academic scope)

        boolean success = userDAO.register(newUser);

        if (success) {
            req.setAttribute("successMsg", "Account created successfully! Please log in.");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        } else {
            req.setAttribute("errorMsg", "Registration failed. Email may already be in use.");
            req.setAttribute("showRegister", true);
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }
}
