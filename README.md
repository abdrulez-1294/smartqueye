# SmartQueue

**SSP Academic Project** — A Java EE Self-Checkout & Queue Management System built with **Servlets, JSP, JDBC, MySQL, Bootstrap 5**, and deployed on **Apache Tomcat 10.1**.

---

## Tech Stack
| Layer | Technology |
|---|---|
| IDE | Eclipse IDE for Enterprise Java |
| Backend | Java EE (Servlets, JSP, HttpSession) |
| Server | Apache Tomcat 10.1 |
| Database | MySQL 8.0 + JDBC |
| Frontend | HTML5, CSS3, Bootstrap 5, JavaScript |

---

## Features
- 🛒 **Scan & Go** — Barcode-based cart building via browser
- 📱 **Session Tracking** — HttpSession-powered live cart
- 🧾 **Digital Invoice** — QR code receipt after checkout
- 🖥️ **Kiosk Interface** — Phone-based cart sync and payment
- ⚖️ **Weight Verification** — Backend logic validates physical bag weight (±50g tolerance)
- 👤 **Admin Panel** — CRUD product inventory with search and modals
- 🔐 **Authentication** — Login, Register, Logout with session management

---

## Quick Start

### 1. Database Setup
Run in MySQL:
```sql
source smartqueue_schema.sql;
```

### 2. Update DB Password
Edit `src/com/smartqueue/util/DBUtil.java`:
```java
private static final String DB_PASS = "your_mysql_password";
```

### 3. Import into Eclipse
- File → Import → Existing Projects into Workspace
- Select root directory → choose `SmartQueue-Eclipse/`
- ✅ "Copy projects into workspace" if needed

### 4. Add JDBC Driver
- Download `mysql-connector-j-8.x.x.jar` from [MVN Repository](https://mvnrepository.com/artifact/com.mysql/mysql-connector-j)
- Copy into `WebContent/WEB-INF/lib/`
- Eclipse auto-detects it on the build path

### 5. Configure Tomcat & Run
- Window → Servers → New → Apache Tomcat 10.1
- Add SmartQueue → Run ▶
- Open: `http://localhost:8080/SmartQueue/`

---

## Default Credentials
| Role | Email | Password | Kiosk Phone |
|---|---|---|---|
| Admin | admin@smartqueue.com | admin123 | — |
| Customer | john@example.com | pass123 | 9876543210 |

---

## Project Structure
```
SmartQueue-Eclipse/
├── .project                      ← Eclipse project descriptor
├── .classpath                    ← Eclipse build path config
├── .settings/                    ← Eclipse WTP facet configs
├── smartqueue_schema.sql         ← MySQL schema + seed data
├── src/
│   └── com/smartqueue/
│       ├── util/    DBUtil.java
│       ├── model/   User.java  Product.java  CartItem.java
│       ├── dao/     UserDAO.java  ProductDAO.java  TransactionDAO.java
│       └── servlet/ AuthServlet.java  CartServlet.java
│                    AdminServlet.java  KioskServlet.java
└── WebContent/
    ├── index.jsp      ← Login / Register
    ├── shop.jsp       ← Scan & Go interface
    ├── invoice.jsp    ← Receipt + QR Code
    ├── kiosk.jsp      ← Kiosk verification terminal
    ├── admin.jsp      ← Admin inventory panel
    ├── css/style.css
    └── WEB-INF/
        ├── web.xml
        └── lib/       ← Place mysql-connector-j.jar here
```

---

## Architecture (MVC)
```
JSP (View) ←→ Servlet (Controller) ←→ DAO (Model) ←→ MySQL
```

**Session keys:** `loggedInUser`, `role`, `cart`, `lastTransactionId`, `checkoutTotal`

---

## Weight Verification Logic
```
expectedWeight = Σ (product.expectedWeightGrams × quantity)
actualWeight   = customer input at kiosk (simulated scale)
verified       = |actualWeight - expectedWeight| ≤ 50g
```
