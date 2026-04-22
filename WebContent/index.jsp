<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- index.jsp — SmartQueue Login & Registration Page --%>
<%-- If user is already logged in, redirect them away from login page --%>
<%
    if (session.getAttribute("loggedInUser") != null) {
        String role = (String) session.getAttribute("role");
        if ("admin".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin");
        } else {
            response.sendRedirect(request.getContextPath() + "/shop.jsp");
        }
        return;
    }
    boolean showRegister = Boolean.TRUE.equals(request.getAttribute("showRegister"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartQueue — Login</title>
    <meta name="description" content="SmartQueue: Scan & Go self-checkout system. Login to start shopping smarter.">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="auth-body">

    <!-- Animated Background Orbs -->
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>

    <div class="auth-container">

        <!-- Brand Header -->
        <div class="auth-brand text-center mb-4">
            <div class="brand-icon-wrap">
                <i class="bi bi-cart-check-fill"></i>
            </div>
            <h1 class="brand-title">SmartQueue</h1>
            <p class="brand-tagline">Scan. Go. No Lines.</p>
        </div>

        <!-- Alert Messages -->
        <% if (request.getAttribute("errorMsg") != null) { %>
        <div class="alert alert-danger alert-dismissible d-flex align-items-center" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <div><%= request.getAttribute("errorMsg") %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>
        <% if (request.getAttribute("successMsg") != null) { %>
        <div class="alert alert-success alert-dismissible d-flex align-items-center" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>
            <div><%= request.getAttribute("successMsg") %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <!-- Tab Navigation -->
        <div class="auth-card">
            <ul class="nav nav-tabs auth-tabs" id="authTabs" role="tablist">
                <li class="nav-item flex-fill" role="presentation">
                    <button class="nav-link auth-tab-btn <%= showRegister ? "" : "active" %>"
                            id="login-tab" data-bs-toggle="tab" data-bs-target="#loginPanel"
                            type="button" role="tab">
                        <i class="bi bi-person-lock me-1"></i> Login
                    </button>
                </li>
                <li class="nav-item flex-fill" role="presentation">
                    <button class="nav-link auth-tab-btn <%= showRegister ? "active" : "" %>"
                            id="register-tab" data-bs-toggle="tab" data-bs-target="#registerPanel"
                            type="button" role="tab">
                        <i class="bi bi-person-plus me-1"></i> Register
                    </button>
                </li>
            </ul>

            <div class="tab-content auth-tab-content p-4">

                <!-- LOGIN PANEL -->
                <div class="tab-pane fade <%= showRegister ? "" : "show active" %>" id="loginPanel" role="tabpanel">
                    <form action="<%= request.getContextPath() %>/login" method="post" id="loginForm" novalidate>
                        <div class="mb-3">
                            <label for="loginEmail" class="form-label">Email Address</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-envelope-fill"></i></span>
                                <input type="email" class="form-control" id="loginEmail" name="email"
                                       placeholder="you@example.com" required autocomplete="email">
                            </div>
                        </div>
                        <div class="mb-4">
                            <label for="loginPassword" class="form-label">Password</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                <input type="password" class="form-control" id="loginPassword" name="password"
                                       placeholder="Enter your password" required autocomplete="current-password">
                                <button class="btn btn-outline-secondary" type="button" id="toggleLoginPwd">
                                    <i class="bi bi-eye-fill"></i>
                                </button>
                            </div>
                        </div>
                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary btn-lg btn-smartqueue" id="loginBtn">
                                <i class="bi bi-box-arrow-in-right me-2"></i>Login
                            </button>
                        </div>
                        <div class="text-center mt-3">
                            <small class="text-muted">Admin? Use your admin credentials above.</small>
                        </div>
                    </form>
                </div>

                <!-- REGISTER PANEL -->
                <div class="tab-pane fade <%= showRegister ? "show active" : "" %>" id="registerPanel" role="tabpanel">
                    <form action="<%= request.getContextPath() %>/register" method="post" id="registerForm" novalidate>
                        <div class="mb-3">
                            <label for="regName" class="form-label">Full Name</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                                <input type="text" class="form-control" id="regName" name="fullName"
                                       placeholder="John Doe" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="regEmail" class="form-label">Email Address</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-envelope-fill"></i></span>
                                <input type="email" class="form-control" id="regEmail" name="email"
                                       placeholder="you@example.com" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="regPhone" class="form-label">Phone Number <span class="text-muted">(used at Kiosk)</span></label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-phone-fill"></i></span>
                                <input type="tel" class="form-control" id="regPhone" name="phone"
                                       placeholder="9876543210" required>
                            </div>
                        </div>
                        <div class="mb-4">
                            <label for="regPassword" class="form-label">Password</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                <input type="password" class="form-control" id="regPassword" name="password"
                                       placeholder="Choose a strong password" required minlength="6">
                                <button class="btn btn-outline-secondary" type="button" id="toggleRegPwd">
                                    <i class="bi bi-eye-fill"></i>
                                </button>
                            </div>
                        </div>
                        <div class="d-grid">
                            <button type="submit" class="btn btn-success btn-lg btn-smartqueue-green" id="registerBtn">
                                <i class="bi bi-person-check-fill me-2"></i>Create Account
                            </button>
                        </div>
                    </form>
                </div>

            </div><!-- end tab-content -->
        </div><!-- end auth-card -->

        <!-- Footer links -->
        <div class="text-center mt-3">
            <a href="<%= request.getContextPath() %>/kiosk" class="auth-footer-link">
                <i class="bi bi-display me-1"></i>Go to Kiosk Interface
            </a>
        </div>

    </div><!-- end auth-container -->

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Password visibility toggles
        document.getElementById('toggleLoginPwd').addEventListener('click', function () {
            const pwd = document.getElementById('loginPassword');
            const icon = this.querySelector('i');
            pwd.type = pwd.type === 'password' ? 'text' : 'password';
            icon.className = pwd.type === 'password' ? 'bi bi-eye-fill' : 'bi bi-eye-slash-fill';
        });
        document.getElementById('toggleRegPwd').addEventListener('click', function () {
            const pwd = document.getElementById('regPassword');
            const icon = this.querySelector('i');
            pwd.type = pwd.type === 'password' ? 'text' : 'password';
            icon.className = pwd.type === 'password' ? 'bi bi-eye-fill' : 'bi bi-eye-slash-fill';
        });
    </script>
</body>
</html>
