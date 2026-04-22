<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="com.smartqueue.model.*, java.util.*" %>
<%-- admin.jsp — SmartQueue Admin Dashboard for Inventory Management --%>
<%
    /* Session Guard — only admins allowed */
    com.smartqueue.model.User adminUser = (com.smartqueue.model.User) session.getAttribute("loggedInUser");
    if (adminUser == null || !"admin".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    @SuppressWarnings("unchecked")
    List<Product> productList = (List<Product>) request.getAttribute("productList");
    if (productList == null) productList = new ArrayList<>();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartQueue — Admin Dashboard</title>
    <meta name="description" content="SmartQueue admin panel for managing product inventory.">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="admin-body">

    <!-- ============ ADMIN SIDEBAR ============ -->
    <div class="admin-layout">
        <aside class="admin-sidebar">
            <div class="sidebar-brand">
                <i class="bi bi-cart-check-fill me-2"></i>SmartQueue
            </div>
            <nav class="sidebar-nav">
                <a href="#" class="sidebar-link active" id="navInventory">
                    <i class="bi bi-box-seam-fill"></i> Inventory
                </a>
                <a href="<%= request.getContextPath() %>/kiosk" class="sidebar-link">
                    <i class="bi bi-display"></i> Kiosk View
                </a>
                <a href="<%= request.getContextPath() %>/logout" class="sidebar-link sidebar-logout">
                    <i class="bi bi-box-arrow-right"></i> Logout
                </a>
            </nav>
            <div class="sidebar-user mt-auto">
                <i class="bi bi-person-badge-fill me-2"></i>
                <div>
                    <div class="fw-semibold"><%= adminUser.getFullName() %></div>
                    <small class="text-muted">Administrator</small>
                </div>
            </div>
        </aside>

        <!-- ============ MAIN CONTENT ============ -->
        <main class="admin-main">

            <!-- Admin Top Bar -->
            <div class="admin-topbar d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="admin-page-title mb-0">Product Inventory</h2>
                    <small class="text-muted"><%= productList.size() %> products in database</small>
                </div>
                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addProductModal">
                    <i class="bi bi-plus-circle-fill me-2"></i>Add Product
                </button>
            </div>

            <!-- Alerts -->
            <% if (request.getAttribute("adminSuccess") != null) { %>
            <div class="alert alert-success alert-dismissible d-flex align-items-center mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <div><%= request.getAttribute("adminSuccess") %></div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>
            <% if (request.getAttribute("adminError") != null) { %>
            <div class="alert alert-danger alert-dismissible d-flex align-items-center mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div><%= request.getAttribute("adminError") %></div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <!-- Stats Cards -->
            <div class="row g-3 mb-4">
                <div class="col-sm-6 col-lg-3">
                    <div class="admin-stat-card">
                        <div class="stat-icon bg-primary-soft"><i class="bi bi-box-seam text-primary"></i></div>
                        <div class="stat-value"><%= productList.size() %></div>
                        <div class="stat-label">Total Products</div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="admin-stat-card">
                        <div class="stat-icon bg-success-soft"><i class="bi bi-check-circle text-success"></i></div>
                        <div class="stat-value">
                            <%= productList.stream().filter(p -> p.getStockQty() > 0).count() %>
                        </div>
                        <div class="stat-label">In Stock</div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="admin-stat-card">
                        <div class="stat-icon bg-warning-soft"><i class="bi bi-exclamation-circle text-warning"></i></div>
                        <div class="stat-value">
                            <%= productList.stream().filter(p -> p.getStockQty() == 0).count() %>
                        </div>
                        <div class="stat-label">Out of Stock</div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="admin-stat-card">
                        <div class="stat-icon bg-info-soft"><i class="bi bi-tags text-info"></i></div>
                        <div class="stat-value">
                            <%= productList.stream().map(Product::getCategory).distinct().count() %>
                        </div>
                        <div class="stat-label">Categories</div>
                    </div>
                </div>
            </div>

            <!-- Products Table -->
            <div class="card admin-table-card">
                <div class="card-body p-0">
                    <!-- Search bar -->
                    <div class="p-3 border-bottom">
                        <input type="text" id="productSearch" class="form-control"
                               placeholder="Search products by name, barcode or category...">
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover admin-table mb-0" id="productTable">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Name</th>
                                    <th>Barcode</th>
                                    <th>Category</th>
                                    <th>Price (₹)</th>
                                    <th>Weight (g)</th>
                                    <th>Stock</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody id="productTableBody">
                                <% if (productList.isEmpty()) { %>
                                <tr>
                                    <td colspan="8" class="text-center py-5 text-muted">
                                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                        No products found. Add your first product!
                                    </td>
                                </tr>
                                <% } else {
                                    int idx = 1;
                                    for (Product p : productList) { %>
                                <tr>
                                    <td><%= idx++ %></td>
                                    <td class="fw-semibold"><%= p.getName() %></td>
                                    <td><span class="barcode-chip"><i class="bi bi-upc me-1"></i><%= p.getBarcode() %></span></td>
                                    <td><span class="category-pill"><%= p.getCategory() %></span></td>
                                    <td>₹<%= String.format("%.2f", p.getPrice()) %></td>
                                    <td><%= p.getExpectedWeightGrams() %>g</td>
                                    <td>
                                        <span class="stock-chip <%= p.getStockQty() > 0 ? "in" : "out" %>">
                                            <%= p.getStockQty() > 0 ? p.getStockQty() + " units" : "Out of Stock" %>
                                        </span>
                                    </td>
                                    <td>
                                        <!-- Edit Button: populates edit modal -->
                                        <button class="btn btn-sm btn-outline-primary me-1"
                                                onclick="openEditModal(<%= p.getProductId() %>, '<%= p.getName().replace("'", "\\'") %>', '<%= p.getBarcode() %>', <%= p.getPrice() %>, <%= p.getExpectedWeightGrams() %>, <%= p.getStockQty() %>, '<%= p.getCategory() %>', '<%= p.getImageUrl() %>')"
                                                title="Edit">
                                            <i class="bi bi-pencil-fill"></i>
                                        </button>
                                        <!-- Delete Button -->
                                        <form action="<%= request.getContextPath() %>/admin" method="post" style="display:inline"
                                              onsubmit="return confirm('Delete <%= p.getName().replace("'", "\\'") %>?')">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="productId" value="<%= p.getProductId() %>">
                                            <button type="submit" class="btn btn-sm btn-outline-danger" title="Delete">
                                                <i class="bi bi-trash-fill"></i>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                                <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </main>
    </div><!-- end admin-layout -->

    <!-- ============ ADD PRODUCT MODAL ============ -->
    <div class="modal fade" id="addProductModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content admin-modal">
                <div class="modal-header">
                    <h5 class="modal-title"><i class="bi bi-plus-circle me-2"></i>Add New Product</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="<%= request.getContextPath() %>/admin" method="post">
                    <input type="hidden" name="action" value="add">
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Product Name <span class="text-danger">*</span></label>
                                <input type="text" name="name" class="form-control" placeholder="e.g. Whole Milk 1L" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Barcode <span class="text-danger">*</span></label>
                                <input type="text" name="barcode" class="form-control" placeholder="e.g. BAR001" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Price (₹) <span class="text-danger">*</span></label>
                                <input type="number" name="price" class="form-control" step="0.01" min="0" placeholder="49.99" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Expected Weight (g) <span class="text-danger">*</span></label>
                                <input type="number" name="expectedWeightGrams" class="form-control" min="0" placeholder="1000" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Stock Quantity <span class="text-danger">*</span></label>
                                <input type="number" name="stockQty" class="form-control" min="0" placeholder="50" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Category <span class="text-danger">*</span></label>
                                <select name="category" class="form-select" required>
                                    <option value="">-- Select Category --</option>
                                    <option>Grocery</option>
                                    <option>Dairy</option>
                                    <option>Beverages</option>
                                    <option>Snacks</option>
                                    <option>Produce</option>
                                    <option>Personal Care</option>
                                    <option>Electronics</option>
                                    <option>Household</option>
                                    <option>Bakery</option>
                                    <option>Meat & Seafood</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Image URL <span class="text-muted">(optional)</span></label>
                                <input type="url" name="imageUrl" class="form-control" placeholder="https://...">
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check2-circle me-1"></i>Add Product
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ============ EDIT PRODUCT MODAL ============ -->
    <div class="modal fade" id="editProductModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content admin-modal">
                <div class="modal-header">
                    <h5 class="modal-title"><i class="bi bi-pencil-square me-2"></i>Edit Product</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="<%= request.getContextPath() %>/admin" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="productId" id="editProductId">
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Product Name</label>
                                <input type="text" name="name" id="editName" class="form-control" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Barcode</label>
                                <input type="text" name="barcode" id="editBarcode" class="form-control" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Price (₹)</label>
                                <input type="number" name="price" id="editPrice" class="form-control" step="0.01" min="0" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Expected Weight (g)</label>
                                <input type="number" name="expectedWeightGrams" id="editWeight" class="form-control" min="0" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Stock Quantity</label>
                                <input type="number" name="stockQty" id="editStock" class="form-control" min="0" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Category</label>
                                <select name="category" id="editCategory" class="form-select" required>
                                    <option>Grocery</option>
                                    <option>Dairy</option>
                                    <option>Beverages</option>
                                    <option>Snacks</option>
                                    <option>Produce</option>
                                    <option>Personal Care</option>
                                    <option>Electronics</option>
                                    <option>Household</option>
                                    <option>Bakery</option>
                                    <option>Meat & Seafood</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Image URL</label>
                                <input type="url" name="imageUrl" id="editImageUrl" class="form-control">
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i>Save Changes
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Populate edit modal with existing product data
        function openEditModal(id, name, barcode, price, weight, stock, category, imageUrl) {
            document.getElementById('editProductId').value  = id;
            document.getElementById('editName').value       = name;
            document.getElementById('editBarcode').value    = barcode;
            document.getElementById('editPrice').value      = price;
            document.getElementById('editWeight').value     = weight;
            document.getElementById('editStock').value      = stock;
            document.getElementById('editImageUrl').value   = imageUrl;
            const select = document.getElementById('editCategory');
            for (let opt of select.options) {
                opt.selected = opt.value === category;
            }
            new bootstrap.Modal(document.getElementById('editProductModal')).show();
        }

        // Live search filter for product table
        document.getElementById('productSearch').addEventListener('input', function () {
            const query = this.value.toLowerCase();
            document.querySelectorAll('#productTableBody tr').forEach(row => {
                row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
            });
        });

        // Auto-dismiss alerts
        setTimeout(() => {
            document.querySelectorAll('.alert').forEach(el => {
                const a = bootstrap.Alert.getOrCreateInstance(el);
                a.close();
            });
        }, 4000);
    </script>
</body>
</html>
