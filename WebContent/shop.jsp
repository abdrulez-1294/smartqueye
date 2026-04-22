<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="com.smartqueue.model.*, com.smartqueue.dao.*, java.util.*" %>
<%-- shop.jsp — Scan & Go Customer Interface --%>
<%
    /* Session Guard — redirect to login if not authenticated */
    com.smartqueue.model.User currentUser = (com.smartqueue.model.User) session.getAttribute("loggedInUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    /* Retrieve cart from session (may be null if empty) */
    @SuppressWarnings("unchecked")
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    if (cart == null) cart = new ArrayList<>();

    /* Retrieve all products for the dropdown/barcode helper */
    ProductDAO productDAO = new ProductDAO();
    List<Product> allProducts = productDAO.getAllProducts();

    /* Calculate cart totals */
    double cartTotal = 0;
    int    cartCount = 0;
    for (CartItem item : cart) {
        cartTotal += item.getLineTotal();
        cartCount += item.getQuantity();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartQueue — Scan & Go</title>
    <meta name="description" content="SmartQueue Scan & Go: Add items by barcode, build your cart, and checkout instantly.">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="shop-body">

    <!-- ==================== TOP NAVBAR ==================== -->
    <nav class="navbar navbar-expand-lg shop-navbar sticky-top">
        <div class="container-fluid px-4">
            <!-- Brand -->
            <a class="navbar-brand shop-brand" href="#">
                <i class="bi bi-cart-check-fill me-2"></i>SmartQueue
                <span class="brand-badge">Scan & Go</span>
            </a>
            <!-- Cart Badge (mobile) -->
            <div class="d-flex align-items-center gap-3 ms-auto">
                <div class="cart-badge-wrap">
                    <i class="bi bi-bag-fill nav-cart-icon"></i>
                    <span class="cart-count-badge"><%= cartCount %></span>
                </div>
                <!-- User dropdown -->
                <div class="dropdown">
                    <button class="btn btn-sm user-pill dropdown-toggle" id="userDropdown"
                            data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-person-circle me-1"></i>
                        <%= currentUser.getFullName().split(" ")[0] %>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end user-dropdown-menu" aria-labelledby="userDropdown">
                        <li class="dropdown-header px-3 py-2">
                            <div class="fw-semibold"><%= currentUser.getFullName() %></div>
                            <small class="text-muted"><%= currentUser.getEmail() %></small>
                        </li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/kiosk">
                            <i class="bi bi-display me-2"></i>Go to Kiosk</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="<%= request.getContextPath() %>/logout">
                            <i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <div class="container-fluid px-4 py-4 shop-main">
        <div class="row g-4">

            <!-- ============= LEFT: SCAN PANEL ============= -->
            <div class="col-lg-7">

                <!-- Scan Form Card -->
                <div class="card shop-card mb-4">
                    <div class="card-body p-4">
                        <h5 class="card-title mb-1">
                            <i class="bi bi-upc-scan me-2 text-primary"></i>Scan Item Barcode
                        </h5>
                        <p class="text-muted small mb-3">Enter the product barcode or select from the dropdown</p>

                        <!-- Scan Alerts -->
                        <% if (request.getAttribute("scanError") != null) { %>
                        <div class="alert alert-danger alert-dismissible d-flex align-items-center mb-3" role="alert">
                            <i class="bi bi-exclamation-octagon-fill me-2"></i>
                            <div><%= request.getAttribute("scanError") %></div>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                        <% } %>
                        <% if (request.getAttribute("scanSuccess") != null) { %>
                        <div class="alert alert-success alert-dismissible d-flex align-items-center mb-3" role="alert">
                            <i class="bi bi-check2-circle me-2"></i>
                            <div><%= request.getAttribute("scanSuccess") %></div>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                        <% } %>

                        <form action="<%= request.getContextPath() %>/cart" method="post" id="scanForm">
                            <input type="hidden" name="action" value="scan">
                            <div class="row g-2">
                                <div class="col-md-7">
                                    <div class="input-group barcode-input-group">
                                        <span class="input-group-text"><i class="bi bi-upc"></i></span>
                                        <input type="text" class="form-control form-control-lg"
                                               id="barcodeInput" name="barcode"
                                               placeholder="e.g. BAR001" autocomplete="off"
                                               list="barcodeList">
                                        <datalist id="barcodeList">
                                            <% for (Product p : allProducts) { %>
                                            <option value="<%= p.getBarcode() %>"><%= p.getName() %></option>
                                            <% } %>
                                        </datalist>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <button type="submit" class="btn btn-primary btn-lg w-100 scan-btn"
                                            id="scanBtn">
                                        <i class="bi bi-plus-circle-fill me-2"></i>Add to Cart
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Product Catalogue Card -->
                <div class="card shop-card">
                    <div class="card-body p-4">
                        <h5 class="card-title mb-3">
                            <i class="bi bi-grid-fill me-2 text-success"></i>Available Products
                        </h5>
                        <div class="row g-3" id="productGrid">
                            <% for (Product p : allProducts) { %>
                            <div class="col-sm-6 col-md-4">
                                <div class="product-tile" onclick="fillBarcode('<%= p.getBarcode() %>')">
                                    <div class="product-tile-body">
                                        <div class="product-category-badge"><%= p.getCategory() %></div>
                                        <h6 class="product-name"><%= p.getName() %></h6>
                                        <div class="product-barcode-label">
                                            <i class="bi bi-upc me-1"></i><%= p.getBarcode() %>
                                        </div>
                                        <div class="product-price-row">
                                            <span class="product-price">₹<%= String.format("%.2f", p.getPrice()) %></span>
                                            <span class="product-weight"><i class="bi bi-speedometer2 me-1"></i><%= p.getExpectedWeightGrams() %>g</span>
                                        </div>
                                        <div class="<%= p.getStockQty() > 0 ? "stock-badge in-stock" : "stock-badge out-stock" %>">
                                            <%= p.getStockQty() > 0 ? "In Stock (" + p.getStockQty() + ")" : "Out of Stock" %>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <% } %>
                            <% if (allProducts.isEmpty()) { %>
                            <div class="col-12 text-center py-4 text-muted">
                                <i class="bi bi-box2 fs-1 d-block mb-2"></i>
                                No products in inventory. Ask admin to add products.
                            </div>
                            <% } %>
                        </div>
                    </div>
                </div>

            </div><!-- end left col -->

            <!-- ============= RIGHT: CART PANEL ============= -->
            <div class="col-lg-5">
                <div class="card shop-card cart-panel sticky-top" style="top: 80px;">
                    <div class="card-body p-0">

                        <!-- Cart Header -->
                        <div class="cart-header p-4">
                            <div class="d-flex justify-content-between align-items-center">
                                <h5 class="mb-0">
                                    <i class="bi bi-bag-fill me-2"></i>Your Cart
                                </h5>
                                <span class="cart-item-count"><%= cartCount %> item<%= cartCount != 1 ? "s" : "" %></span>
                            </div>
                        </div>

                        <!-- Cart Items List -->
                        <div class="cart-items-list px-4">
                            <% if (cart.isEmpty()) { %>
                            <div class="cart-empty-state text-center py-5">
                                <i class="bi bi-bag-x fs-1 d-block mb-3 text-muted"></i>
                                <p class="text-muted">Your cart is empty.<br>Scan an item to get started!</p>
                            </div>
                            <% } else {
                                for (CartItem item : cart) { %>
                            <div class="cart-item-row d-flex align-items-center py-3">
                                <div class="cart-item-icon me-3">
                                    <i class="bi bi-box-seam"></i>
                                </div>
                                <div class="cart-item-info flex-grow-1">
                                    <div class="cart-item-name"><%= item.getProduct().getName() %></div>
                                    <div class="cart-item-meta">
                                        <span>Qty: <%= item.getQuantity() %></span>
                                        <span class="mx-2">·</span>
                                        <span>₹<%= String.format("%.2f", item.getProduct().getPrice()) %> each</span>
                                        <span class="mx-2">·</span>
                                        <span><%= item.getLineTotalWeightGrams() %>g</span>
                                    </div>
                                </div>
                                <div class="cart-item-total me-3">
                                    ₹<%= String.format("%.2f", item.getLineTotal()) %>
                                </div>
                                <!-- Remove Button -->
                                <form action="<%= request.getContextPath() %>/cart" method="post" style="display:inline">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="barcode" value="<%= item.getProduct().getBarcode() %>">
                                    <button type="submit" class="btn btn-sm btn-remove" title="Remove item">
                                        <i class="bi bi-trash3-fill"></i>
                                    </button>
                                </form>
                            </div>
                            <% } } %>
                        </div>

                        <!-- Cart Footer: Total + Actions -->
                        <div class="cart-footer p-4">
                            <div class="d-flex justify-content-between align-items-center mb-3 cart-total-row">
                                <span class="cart-total-label">Grand Total</span>
                                <span class="cart-total-amount">₹<%= String.format("%.2f", cartTotal) %></span>
                            </div>

                            <!-- Checkout Button -->
                            <% if (!cart.isEmpty()) { %>
                            <form action="<%= request.getContextPath() %>/cart" method="post" class="d-grid mb-2">
                                <input type="hidden" name="action" value="checkout">
                                <button type="submit" class="btn btn-lg btn-checkout" id="checkoutBtn">
                                    <i class="bi bi-qr-code me-2"></i>Checkout & Get QR Code
                                </button>
                            </form>
                            <form action="<%= request.getContextPath() %>/cart" method="post" class="d-grid">
                                <input type="hidden" name="action" value="clear">
                                <button type="submit" class="btn btn-outline-danger btn-sm">
                                    <i class="bi bi-trash me-1"></i>Clear Cart
                                </button>
                            </form>
                            <% } %>
                        </div>

                    </div>
                </div>
            </div><!-- end right col -->

        </div><!-- end row -->
    </div><!-- end container -->

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Auto-fill barcode input when clicking a product tile
        function fillBarcode(barcode) {
            document.getElementById('barcodeInput').value = barcode;
            document.getElementById('barcodeInput').focus();
            // Smooth scroll to scan form on mobile
            document.getElementById('scanForm').scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        // Auto-dismiss alerts after 4 seconds
        setTimeout(() => {
            document.querySelectorAll('.alert').forEach(el => {
                const bsAlert = bootstrap.Alert.getOrCreateInstance(el);
                bsAlert.close();
            });
        }, 4000);

        // Visual feedback on scan submit
        document.getElementById('scanForm').addEventListener('submit', function () {
            const btn = document.getElementById('scanBtn');
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Scanning...';
            btn.disabled = true;
        });
    </script>
</body>
</html>
