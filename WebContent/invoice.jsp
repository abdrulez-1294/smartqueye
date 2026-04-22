<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="com.smartqueue.model.*, java.util.*" %>
<%-- invoice.jsp — Post-checkout Digital Receipt & QR Code Page --%>
<%
    /* Session Guard */
    com.smartqueue.model.User currentUser = (com.smartqueue.model.User) session.getAttribute("loggedInUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }

    Integer txId     = (Integer) session.getAttribute("lastTransactionId");
    Double  total    = (Double)  session.getAttribute("checkoutTotal");

    /* Redirect back to shop if no transaction exists */
    if (txId == null || total == null) {
        response.sendRedirect(request.getContextPath() + "/shop.jsp");
        return;
    }

    @SuppressWarnings("unchecked")
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    if (cart == null) cart = new ArrayList<>();

    /* Generate a time-stamped receipt number */
    String receiptNo = "SQ-" + txId + "-" + System.currentTimeMillis() % 10000;
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartQueue — Receipt #<%= txId %></title>
    <meta name="description" content="SmartQueue digital receipt. Show this QR code at the kiosk to exit.">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <!-- QR Code generation library -->
    <script src="https://cdn.jsdelivr.net/npm/qrcodejs@1.0.0/qrcode.min.js"></script>
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="invoice-body">

    <!-- Animated confetti particles (pure CSS) -->
    <div class="confetti-container" id="confettiContainer"></div>

    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-7 col-md-9">

                <!-- Success Banner -->
                <div class="text-center mb-4">
                    <div class="success-icon-wrap">
                        <i class="bi bi-check-circle-fill success-icon"></i>
                    </div>
                    <h1 class="success-title">Checkout Complete!</h1>
                    <p class="success-subtitle">Show the QR code below at the kiosk to exit the store.</p>
                </div>

                <!-- Receipt Card -->
                <div class="card invoice-card">
                    <div class="card-body p-0">

                        <!-- Receipt Header -->
                        <div class="invoice-header p-4">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h4 class="mb-0"><i class="bi bi-cart-check-fill me-2"></i>SmartQueue</h4>
                                    <small>Digital Receipt</small>
                                </div>
                                <div class="text-end">
                                    <div class="receipt-no">#<%= receiptNo %></div>
                                    <small><%= fmt.format(now) %></small>
                                </div>
                            </div>
                        </div>

                        <!-- Customer Info -->
                        <div class="px-4 py-3 border-bottom invoice-section">
                            <div class="row">
                                <div class="col-6">
                                    <label class="receipt-label">Customer</label>
                                    <div class="receipt-value"><%= currentUser.getFullName() %></div>
                                </div>
                                <div class="col-6">
                                    <label class="receipt-label">Phone</label>
                                    <div class="receipt-value"><%= currentUser.getPhone() %></div>
                                </div>
                            </div>
                        </div>

                        <!-- Items Table -->
                        <div class="px-4 py-3 border-bottom">
                            <table class="table table-sm invoice-table mb-0">
                                <thead>
                                    <tr>
                                        <th>Item</th>
                                        <th class="text-center">Qty</th>
                                        <th class="text-end">Unit</th>
                                        <th class="text-end">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (CartItem item : cart) { %>
                                    <tr>
                                        <td><%= item.getProduct().getName() %></td>
                                        <td class="text-center"><%= item.getQuantity() %></td>
                                        <td class="text-end">₹<%= String.format("%.2f", item.getProduct().getPrice()) %></td>
                                        <td class="text-end fw-semibold">₹<%= String.format("%.2f", item.getLineTotal()) %></td>
                                    </tr>
                                    <% } %>
                                </tbody>
                                <tfoot>
                                    <tr class="invoice-total-row">
                                        <td colspan="3" class="text-end fw-bold">Grand Total</td>
                                        <td class="text-end fw-bold invoice-grand-total">₹<%= String.format("%.2f", total) %></td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>

                        <!-- QR Code Section -->
                        <div class="p-4 text-center">
                            <p class="text-muted small mb-3">
                                <i class="bi bi-qr-code me-1"></i>
                                Scan this QR code at the kiosk gate. Transaction ID: <strong><%= txId %></strong>
                            </p>
                            <div class="qr-wrapper mx-auto" id="qrCode"></div>
                            <div class="mt-3">
                                <span class="status-badge status-pending">
                                    <i class="bi bi-hourglass-split me-1"></i>Pending Kiosk Verification
                                </span>
                            </div>
                        </div>

                        <!-- Action Buttons -->
                        <div class="invoice-actions p-4 d-flex gap-3 justify-content-center flex-wrap">
                            <a href="<%= request.getContextPath() %>/kiosk" class="btn btn-primary btn-lg btn-smartqueue">
                                <i class="bi bi-display me-2"></i>Go to Kiosk
                            </a>
                            <a href="<%= request.getContextPath() %>/shop.jsp" class="btn btn-outline-secondary btn-lg">
                                <i class="bi bi-cart-plus me-2"></i>Shop More
                            </a>
                        </div>

                    </div>
                </div><!-- end invoice-card -->

            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Generate QR code with transaction ID embedded
        const qrData = JSON.stringify({
            txId:      <%= txId %>,
            user:      '<%= currentUser.getEmail() %>',
            total:     '<%= String.format("%.2f", total) %>',
            timestamp: '<%= fmt.format(now) %>'
        });

        new QRCode(document.getElementById("qrCode"), {
            text:          qrData,
            width:         180,
            height:        180,
            colorDark:     "#1a1a2e",
            colorLight:    "#ffffff",
            correctLevel:  QRCode.CorrectLevel.H
        });

        // CSS Confetti animation
        function launchConfetti() {
            const colors = ['#6c63ff', '#f093fb', '#f5576c', '#4facfe', '#43e97b', '#fa709a'];
            const container = document.getElementById('confettiContainer');
            for (let i = 0; i < 60; i++) {
                const dot = document.createElement('div');
                dot.className = 'confetti-piece';
                dot.style.left        = Math.random() * 100 + 'vw';
                dot.style.background  = colors[Math.floor(Math.random() * colors.length)];
                dot.style.animationDelay    = Math.random() * 2 + 's';
                dot.style.animationDuration = (Math.random() * 2 + 2) + 's';
                dot.style.width  = Math.random() * 10 + 5 + 'px';
                dot.style.height = dot.style.width;
                dot.style.borderRadius = Math.random() > 0.5 ? '50%' : '2px';
                container.appendChild(dot);
            }
            setTimeout(() => container.innerHTML = '', 5000);
        }
        launchConfetti();
    </script>
</body>
</html>
