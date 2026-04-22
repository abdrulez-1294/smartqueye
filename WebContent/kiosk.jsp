<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="com.smartqueue.model.*, java.util.*" %>
<%-- kiosk.jsp — SmartQueue Kiosk Hardware Simulation Interface --%>
<%
    /* Extract kiosk-specific session/request attributes */
    com.smartqueue.model.User kioskUser  = (com.smartqueue.model.User)  request.getAttribute("kioskUser");
    @SuppressWarnings("unchecked")
    List<CartItem>  kioskCart  = (List<CartItem>)  request.getAttribute("kioskCart");
    Integer         kioskTxId  = (Integer)          request.getAttribute("kioskTxId");
    Boolean         verSuccess = (Boolean)           request.getAttribute("verificationSuccess");
    String          kioskMsg   = (String)            request.getAttribute("kioskMsg");
    String          kioskError = (String)            request.getAttribute("kioskError");
    Integer         expWeight  = (Integer)           request.getAttribute("expectedWeight");
    Integer         actWeight  = (Integer)           request.getAttribute("actualWeight");

    double kioskTotal = 0;
    int    kioskTotalWeight = 0;
    if (kioskCart != null) {
        for (CartItem ci : kioskCart) {
            kioskTotal       += ci.getLineTotal();
            kioskTotalWeight += ci.getLineTotalWeightGrams();
        }
    }

    // Determine kiosk state
    String state = "PHONE"; // PHONE → CART → VERIFY → SUCCESS
    if (verSuccess != null && verSuccess) state = "SUCCESS";
    else if (kioskUser != null && kioskCart != null) state = "VERIFY";
    else if (kioskUser != null) state = "NO_CART";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartQueue — Kiosk</title>
    <meta name="description" content="SmartQueue kiosk self-checkout verification terminal.">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="kiosk-body">

    <!-- Kiosk Frame -->
    <div class="kiosk-frame">

        <!-- Kiosk Header -->
        <div class="kiosk-header">
            <div class="kiosk-logo">
                <i class="bi bi-cart-check-fill me-2"></i>SmartQueue
            </div>
            <div class="kiosk-subtitle">Self-Checkout Verification Kiosk</div>
            <div class="kiosk-time" id="kioskTime"></div>
        </div>

        <!-- ====================== STATE: PHONE LOOKUP ====================== -->
        <% if ("PHONE".equals(state)) { %>
        <div class="kiosk-content">
            <div class="kiosk-section-title">
                <i class="bi bi-phone-fill me-2"></i>Enter Your Phone Number
            </div>
            <p class="text-center text-muted mb-4">
                Use the phone number linked to your SmartQueue account to sync your cart.
            </p>

            <% if (kioskError != null) { %>
            <div class="kiosk-alert kiosk-alert-danger mb-4">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <%= kioskError %>
            </div>
            <% } %>

            <form action="<%= request.getContextPath() %>/kiosk" method="post" class="kiosk-form">
                <input type="hidden" name="action" value="lookup">
                <div class="kiosk-input-group mb-4">
                    <i class="bi bi-phone-fill kiosk-input-icon"></i>
                    <input type="tel" name="phone" id="kioskPhone"
                           class="kiosk-input" placeholder="e.g. 9876543210"
                           maxlength="12" autofocus required>
                </div>
                <!-- Large Numpad for Kiosk Touchscreen Simulation -->
                <div class="kiosk-numpad mb-4" id="numpad">
                    <% String[] numKeys = {"1","2","3","4","5","6","7","8","9","CLR","0","⌫"}; %>
                    <% for (String k : numKeys) { %>
                    <button type="button" class="numpad-key" onclick="numpadPress('<%= k %>')"><%= k %></button>
                    <% } %>
                </div>
                <button type="submit" class="btn btn-kiosk-primary w-100">
                    <i class="bi bi-search me-2"></i>Find My Account
                </button>
            </form>

            <div class="text-center mt-4">
                <a href="<%= request.getContextPath() %>/index.jsp" class="kiosk-back-link">
                    <i class="bi bi-arrow-left me-1"></i>Back to Login
                </a>
            </div>
        </div>

        <!-- ====================== STATE: CART VERIFICATION ====================== -->
        <% } else if ("VERIFY".equals(state)) { %>
        <div class="kiosk-content">
            <!-- Welcome Banner -->
            <div class="kiosk-welcome-banner mb-4">
                <i class="bi bi-person-check-fill me-2"></i>
                Welcome, <strong><%= kioskUser.getFullName() %></strong>!
                <span class="ms-3 text-muted">TX: #<%= kioskTxId %></span>
            </div>

            <!-- Error Alert -->
            <% if (kioskError != null) { %>
            <div class="kiosk-alert kiosk-alert-danger mb-4">
                <i class="bi bi-exclamation-octagon-fill me-2 fs-5"></i>
                <%= kioskError %>
            </div>
            <% } %>

            <!-- Cart Summary -->
            <div class="kiosk-cart-table mb-4">
                <div class="kiosk-cart-header d-flex justify-content-between px-3 py-2">
                    <span><i class="bi bi-bag-fill me-2"></i>Scanned Items</span>
                    <span><%= kioskCart.size() %> item(s)</span>
                </div>
                <% for (CartItem ci : kioskCart) { %>
                <div class="kiosk-cart-row d-flex align-items-center px-3 py-2">
                    <div class="flex-grow-1">
                        <span class="kiosk-item-name"><%= ci.getProduct().getName() %></span>
                        <span class="kiosk-item-qty ms-2 text-muted">× <%= ci.getQuantity() %></span>
                    </div>
                    <div class="kiosk-item-weight text-muted me-3">
                        <i class="bi bi-speedometer2 me-1"></i><%= ci.getLineTotalWeightGrams() %>g
                    </div>
                    <div class="kiosk-item-price">₹<%= String.format("%.2f", ci.getLineTotal()) %></div>
                </div>
                <% } %>
                <div class="kiosk-cart-footer d-flex justify-content-between px-3 py-3">
                    <span class="fw-bold">Expected Total Weight</span>
                    <span class="kiosk-expected-weight"><i class="bi bi-speedometer2 me-1"></i><%= kioskTotalWeight %>g</span>
                </div>
                <div class="kiosk-cart-footer d-flex justify-content-between px-3 pb-3">
                    <span class="fw-bold">Amount to Pay</span>
                    <span class="kiosk-grand-total">₹<%= String.format("%.2f", kioskTotal) %></span>
                </div>
            </div>

            <!-- Weight Verification Form -->
            <div class="kiosk-verify-box mb-4">
                <div class="kiosk-verify-title">
                    <i class="bi bi-speedometer2 me-2"></i>Place Your Bag on the Scale
                </div>
                <p class="text-muted small text-center">Enter the weight shown on the scale display (in grams)</p>
                <form action="<%= request.getContextPath() %>/kiosk" method="post" id="verifyForm">
                    <input type="hidden" name="action" value="verify">
                    <div class="kiosk-weight-input-wrap">
                        <input type="number" name="actualWeightGrams" id="weightInput"
                               class="kiosk-weight-input" placeholder="Enter grams"
                               min="0" max="99999" required>
                        <span class="kiosk-weight-unit">grams</span>
                    </div>
                    <!-- Quick fill buttons (simulating scale readings) -->
                    <div class="d-flex gap-2 justify-content-center mt-3 mb-3 flex-wrap">
                        <% if (kioskTotalWeight > 0) { %>
                        <button type="button" class="btn btn-sm btn-outline-success"
                                onclick="document.getElementById('weightInput').value = '<%= kioskTotalWeight %>'">
                            <i class="bi bi-check me-1"></i>Use Expected (<%= kioskTotalWeight %>g)
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-warning"
                                onclick="document.getElementById('weightInput').value = '<%= kioskTotalWeight + 200 %>'">
                            <i class="bi bi-exclamation me-1"></i>Simulate Mismatch
                        </button>
                        <% } %>
                    </div>
                    <button type="submit" class="btn btn-kiosk-verify w-100">
                        <i class="bi bi-shield-check-fill me-2"></i>Verify & Confirm Payment
                    </button>
                </form>
            </div>

        </div>

        <!-- ====================== STATE: NO CART (user found but no transaction) ====================== -->
        <% } else if ("NO_CART".equals(state)) { %>
        <div class="kiosk-content text-center">
            <div class="kiosk-alert kiosk-alert-warning mb-4">
                <%= kioskError != null ? kioskError : "No pending transaction found." %>
            </div>
            <a href="<%= request.getContextPath() %>/shop.jsp" class="btn btn-kiosk-primary me-3">
                <i class="bi bi-cart me-2"></i>Go to Scan & Go
            </a>
            <a href="<%= request.getContextPath() %>/kiosk" class="btn btn-outline-light">
                <i class="bi bi-arrow-clockwise me-2"></i>Try Again
            </a>
        </div>

        <!-- ====================== STATE: SUCCESS ====================== -->
        <% } else if ("SUCCESS".equals(state)) { %>
        <div class="kiosk-content text-center">
            <div class="kiosk-success-icon">
                <i class="bi bi-check-circle-fill"></i>
            </div>
            <h2 class="kiosk-success-title"><%= kioskMsg %></h2>
            <p class="text-muted mb-4">
                Weight match confirmed. Transaction #<%= request.getAttribute("transactionId") %> marked as PAID.
            </p>

            <!-- Weight Summary -->
            <% if (expWeight != null && actWeight != null) { %>
            <div class="kiosk-weight-summary mb-4">
                <div class="weight-row">
                    <span>Expected Weight</span>
                    <span class="fw-bold"><%= expWeight %>g</span>
                </div>
                <div class="weight-row">
                    <span>Actual Weight</span>
                    <span class="fw-bold"><%= actWeight %>g</span>
                </div>
                <div class="weight-row text-success">
                    <span>Difference</span>
                    <span class="fw-bold"><%= Math.abs(actWeight - expWeight) %>g ✓</span>
                </div>
            </div>
            <% } %>

            <div class="kiosk-gate-icon">
                <i class="bi bi-door-open-fill"></i>
            </div>
            <p class="kiosk-gate-text">Gate is Open — Thank you for shopping!</p>

            <a href="<%= request.getContextPath() %>/kiosk" class="btn btn-outline-light mt-3">
                <i class="bi bi-arrow-clockwise me-2"></i>Next Customer
            </a>
        </div>
        <% } %>

        <!-- Kiosk Footer -->
        <div class="kiosk-footer">
            <span><i class="bi bi-shield-lock-fill me-1"></i>Secure Checkout</span>
            <span class="mx-3">·</span>
            <span><i class="bi bi-wifi me-1"></i>Online</span>
            <span class="mx-3">·</span>
            <a href="<%= request.getContextPath() %>/index.jsp" class="text-muted text-decoration-none">
                SmartQueue v1.0
            </a>
        </div>

    </div><!-- end kiosk-frame -->

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Live clock for kiosk display
        function updateClock() {
            const el = document.getElementById('kioskTime');
            if (el) {
                el.textContent = new Date().toLocaleTimeString('en-IN', {
                    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true
                });
            }
        }
        updateClock();
        setInterval(updateClock, 1000);

        // Touchscreen numpad logic
        function numpadPress(key) {
            const input = document.getElementById('kioskPhone');
            if (!input) return;
            if (key === 'CLR') {
                input.value = '';
            } else if (key === '⌫') {
                input.value = input.value.slice(0, -1);
            } else {
                if (input.value.length < 12) {
                    input.value += key;
                }
            }
        }

        // Auto-advance gate animation on success
        <% if ("SUCCESS".equals(state)) { %>
        setTimeout(() => {
            document.querySelector('.kiosk-gate-icon').classList.add('gate-open-anim');
        }, 500);
        <% } %>
    </script>
</body>
</html>
