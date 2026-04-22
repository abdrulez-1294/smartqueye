-- ============================================================
-- SmartQueue Database Schema
-- Run this entire script in MySQL Workbench or CLI:
--   mysql -u root -p < smartqueue_schema.sql
-- ============================================================

-- 1. Create and select the database
CREATE DATABASE IF NOT EXISTS smartqueue_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smartqueue_db;

-- ============================================================
-- TABLE: users
-- Stores customer and admin accounts
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(15)  NOT NULL UNIQUE,   -- Used for Kiosk login
    password_hash VARCHAR(255) NOT NULL,           -- Plain text for academic scope
    role          ENUM('customer','admin') NOT NULL DEFAULT 'customer',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: products
-- Stores retail products with weight for kiosk verification
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    product_id             INT            NOT NULL AUTO_INCREMENT,
    name                   VARCHAR(150)   NOT NULL,
    barcode                VARCHAR(50)    NOT NULL UNIQUE,
    price                  DECIMAL(10,2)  NOT NULL,
    expected_weight_grams  INT            NOT NULL DEFAULT 0,  -- Weight in grams
    stock_qty              INT            NOT NULL DEFAULT 0,
    category               VARCHAR(80)    NOT NULL DEFAULT 'General',
    image_url              VARCHAR(500)            DEFAULT '',
    created_at             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    INDEX idx_barcode (barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: transactions
-- Stores checkout session headers
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id  INT            NOT NULL AUTO_INCREMENT,
    user_id         INT            NOT NULL,
    total_amount    DECIMAL(10,2)  NOT NULL,
    payment_status  ENUM('PENDING','PAID','FAILED') NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at         TIMESTAMP      NULL DEFAULT NULL,
    PRIMARY KEY (transaction_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLE: transaction_items
-- Individual line items for each transaction
-- ============================================================
CREATE TABLE IF NOT EXISTS transaction_items (
    item_id         INT           NOT NULL AUTO_INCREMENT,
    transaction_id  INT           NOT NULL,
    product_id      INT           NOT NULL,
    quantity        INT           NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (item_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)     REFERENCES products(product_id)         ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SEED DATA: Admin user
-- Login: admin@smartqueue.com / admin123
-- ============================================================
INSERT INTO users (full_name, email, phone, password_hash, role) VALUES
('Admin User', 'admin@smartqueue.com', '0000000000', 'admin123', 'admin')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- SEED DATA: Demo customer
-- Login: john@example.com / pass123   |  Kiosk Phone: 9876543210
INSERT INTO users (full_name, email, phone, password_hash, role) VALUES
('John Doe', 'john@example.com', '9876543210', 'pass123', 'customer')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- ============================================================
-- SEED DATA: Sample products with realistic weights
-- ============================================================
INSERT INTO products (name, barcode, price, expected_weight_grams, stock_qty, category) VALUES
('Whole Milk 1L',          'BAR001', 55.00,  1050, 100, 'Dairy'),
('Brown Bread Loaf',       'BAR002', 35.00,   450,  80, 'Bakery'),
('Basmati Rice 1kg',       'BAR003', 85.00,  1000,  60, 'Grocery'),
('Organic Eggs (6 pack)',  'BAR004', 65.00,   360,  50, 'Dairy'),
('Amul Butter 500g',       'BAR005', 240.00,  500,  40, 'Dairy'),
('Lays Classic Chips',     'BAR006', 20.00,   52,  150, 'Snacks'),
('Coca-Cola 750ml',        'BAR007', 45.00,   770,  90, 'Beverages'),
('Tata Salt 1kg',          'BAR008', 22.00,  1000, 120, 'Grocery'),
('Colgate Toothpaste 150g','BAR009', 55.00,   150,  70, 'Personal Care'),
('Sunflower Oil 1L',       'BAR010', 120.00, 920,   45, 'Grocery'),
('Maggi Noodles 70g',      'BAR011', 14.00,   70,  200, 'Grocery'),
('Tropicana Orange 1L',    'BAR012', 110.00,  1100, 55, 'Beverages')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================
-- Verification query — run after seeding to confirm data
-- ============================================================
SELECT 'Users:'    AS tbl, COUNT(*) AS cnt FROM users
UNION ALL
SELECT 'Products:', COUNT(*) FROM products;
