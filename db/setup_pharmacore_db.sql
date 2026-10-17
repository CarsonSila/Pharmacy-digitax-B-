-- =====================================================================
-- PharmaCore POS: Tax-Compliant Retail Pharmacy Database Schema
-- Compatible with MySQL 8.0+
-- Jurisdiction: Kenya (KRA eTIMS via DigiTax) / General Retail Pharmacy
-- =====================================================================

CREATE DATABASE IF NOT EXISTS pharmacore_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE pharmacore_db;

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,       -- 'ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER'
    description VARCHAR(255) NULL
) ENGINE=InnoDB;

INSERT INTO roles (role_id, name, description) VALUES
(1, 'ROLE_ADMIN', 'System Administrator with full access'),
(2, 'ROLE_PHARMACIST', 'Licensed Pharmacist - manages inventory, prescriptions & batches'),
(3, 'ROLE_CASHIER', 'POS Cashier - counter sales and receipt issuance')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(30) NULL,
    password_hash VARCHAR(255) NOT NULL,
    pin_code VARCHAR(10) NULL,             -- Quick login PIN for POS cashier terminal
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB;

-- Default Admin User (Password: admin123 -> BCrypt hash or initial placeholder)
INSERT INTO users (user_id, role_id, username, full_name, email, password_hash, is_active) VALUES
(1, 1, 'admin', 'System Administrator', 'admin@pharmacore.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 3. Tax Classifications Table (eTIMS / DigiTax Tax Categories)
CREATE TABLE IF NOT EXISTS tax_classifications (
    tax_class_id INT AUTO_INCREMENT PRIMARY KEY,
    class_code VARCHAR(30) NOT NULL UNIQUE,      -- 'EXEMPT', 'ZERO_RATED', 'VAT_16'
    tax_name VARCHAR(100) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00, -- 0.00, 16.00
    kra_tax_code VARCHAR(10) NOT NULL,          -- 'A' for 16%, 'B' for 0%, 'E' for Exempt
    legal_description VARCHAR(255) NULL
) ENGINE=InnoDB;

INSERT INTO tax_classifications (tax_class_id, class_code, tax_name, tax_rate, kra_tax_code, legal_description) VALUES
(1, 'EXEMPT', 'Exempt Pharmaceuticals', 0.00, 'E', 'Medicaments and pharmaceuticals under VAT Act First Schedule'),
(2, 'ZERO_RATED', 'Zero-Rated Medical Supplies', 0.00, 'B', 'Specific medical supplies and exports under Second Schedule'),
(3, 'VAT_16', 'Standard Rate 16% VAT', 16.00, 'A', 'Cosmetics, toiletries, supplements, non-exempt OTC items')
ON DUPLICATE KEY UPDATE tax_rate=VALUES(tax_rate);

-- 4. Product Categories Table
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) UNIQUE,
    description VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO categories (category_id, name, code, description) VALUES
(1, 'Prescription Medicines (POM)', 'POM', 'Antibiotics, antihypertensives, controlled medicines'),
(2, 'Over The Counter (OTC)', 'OTC', 'Pain relief, cold & flu, antacids'),
(3, 'Vitamins & Supplements', 'SUPP', 'Multivitamins, minerals, dietary supplements'),
(4, 'Personal Care & Beauty', 'BEAUTY', 'Skincare, sunscreens, toiletries'),
(5, 'Medical Supplies & Devices', 'DEVICES', 'Thermometers, bandages, syringes')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 5. Units of Measure Table
CREATE TABLE IF NOT EXISTS units_of_measure (
    unit_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,   -- 'Box', 'Blister', 'Tablet', 'Bottle', 'Tube'
    symbol VARCHAR(20) NOT NULL,        -- 'box', 'strip', 'tab', 'btl'
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO units_of_measure (unit_id, name, symbol) VALUES
(1, 'Tablet/Capsule', 'tab'),
(2, 'Blister/Strip', 'strip'),
(3, 'Box/Pack', 'box'),
(4, 'Bottle', 'btl'),
(5, 'Tube', 'tube'),
(6, 'Sachet', 'sachet')
ON DUPLICATE KEY UPDATE symbol=VALUES(symbol);

-- 6. Products Table
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100) UNIQUE,
    brand_name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255) NULL,
    category_id INT NOT NULL,
    base_unit_id INT NOT NULL,              -- Smallest inventory unit (e.g. tablet)
    tax_class_id INT NOT NULL,             -- FK -> tax_classifications
    hs_code VARCHAR(50) NOT NULL,          -- e.g. '3004.10.00' for penicillin medicaments
    prescription_required TINYINT(1) DEFAULT 0,
    cost_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    reorder_level INT DEFAULT 20,
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (base_unit_id) REFERENCES units_of_measure(unit_id),
    FOREIGN KEY (tax_class_id) REFERENCES tax_classifications(tax_class_id),
    INDEX idx_barcode (barcode),
    INDEX idx_sku (sku)
) ENGINE=InnoDB;

-- 7. Product Unit Conversions (Selling by Box vs Strip vs Tablet)
CREATE TABLE IF NOT EXISTS product_unit_conversions (
    conversion_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    unit_id INT NOT NULL,
    conversion_factor INT NOT NULL,        -- e.g., 1 Box = 100 Tablets; 1 Strip = 10 Tablets
    selling_price DECIMAL(12,2) NOT NULL,  -- Specific price when sold in this unit
    barcode VARCHAR(100) NULL,             -- Unique barcode for the box/strip
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES units_of_measure(unit_id),
    UNIQUE KEY uq_prod_unit (product_id, unit_id)
) ENGINE=InnoDB;

-- 8. Product Batches (FEFO Inventory)
CREATE TABLE IF NOT EXISTS product_batches (
    batch_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    batch_number VARCHAR(100) NOT NULL,
    manufacturing_date DATE NULL,
    expiry_date DATE NOT NULL,
    cost_price DECIMAL(12,2) NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0, -- Stored in base units (e.g. tablets)
    supplier_name VARCHAR(150) NULL,
    is_quarantined TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_fefo (product_id, expiry_date ASC, quantity_available)
) ENGINE=InnoDB;

-- 9. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NULL,
    email VARCHAR(150) NULL,
    kra_pin VARCHAR(30) NULL,               -- Required for B2B / corporate tax invoices
    loyalty_points INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cust_phone (phone),
    INDEX idx_cust_pin (kra_pin)
) ENGINE=InnoDB;

-- 10. Orders Table (Sales Counter Header)
CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,  -- 'ORD-20261017-0001'
    cashier_id INT NOT NULL,
    customer_id INT NULL,
    prescription_reference VARCHAR(100) NULL,
    prescribing_doctor VARCHAR(150) NULL,
    patient_name VARCHAR(150) NULL,
    subtotal_amount DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    payment_method ENUM('CASH', 'MPESA', 'CARD', 'INSURANCE', 'SPLIT') NOT NULL DEFAULT 'CASH',
    payment_reference VARCHAR(100) NULL,      -- e.g. M-Pesa Transaction ID: SHK18928JD
    payment_status ENUM('PAID', 'PENDING', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PAID',
    notes VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cashier_id) REFERENCES users(user_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) ENGINE=InnoDB;

-- 11. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id INT NOT NULL,
    batch_id BIGINT NOT NULL,
    unit_id INT NOT NULL,
    quantity INT NOT NULL,                     -- Quantity sold in selected unit
    quantity_in_base_units INT NOT NULL,       -- Total base units deducted from batch
    unit_price DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_class_id INT NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    tax_amount DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (batch_id) REFERENCES product_batches(batch_id),
    FOREIGN KEY (unit_id) REFERENCES units_of_measure(unit_id),
    FOREIGN KEY (tax_class_id) REFERENCES tax_classifications(tax_class_id)
) ENGINE=InnoDB;

-- 12. Fiscal Invoices Table (DigiTax / KRA eTIMS Ledger)
CREATE TABLE IF NOT EXISTS fiscal_invoices (
    fiscal_invoice_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    branch_code VARCHAR(50) NOT NULL DEFAULT 'MAIN_BRANCH',
    cashier_name VARCHAR(150) NOT NULL,
    buyer_pin VARCHAR(30) NULL,
    buyer_name VARCHAR(150) NOT NULL DEFAULT 'Walk-in Retail',
    buyer_phone VARCHAR(30) NULL,
    total_taxable_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00, -- vatable net
    total_exempt_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,  -- exempt total
    total_zero_rated_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_vat_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(12,2) NOT NULL,
    kra_control_unit_number VARCHAR(100) NULL,
    kra_invoice_number VARCHAR(100) NULL,
    qr_code_string TEXT NULL,
    qr_code_url VARCHAR(500) NULL,
    fiscal_status ENUM('QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'QUEUED',
    retry_count INT NOT NULL DEFAULT 0,
    last_error_message TEXT NULL,
    fiscalized_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX idx_fiscal_status (fiscal_status, retry_count)
) ENGINE=InnoDB;

-- 13. Fiscal Audit Logs Table
CREATE TABLE IF NOT EXISTS fiscal_audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fiscal_invoice_id BIGINT NOT NULL,
    request_endpoint VARCHAR(255) NOT NULL,
    request_payload JSON NULL,
    response_payload JSON NULL,
    http_status_code INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fiscal_invoice_id) REFERENCES fiscal_invoices(fiscal_invoice_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. Credit Notes / Returns Table
CREATE TABLE IF NOT EXISTS credit_notes (
    credit_note_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_order_id BIGINT NOT NULL,
    original_fiscal_invoice_id BIGINT NOT NULL,
    credit_note_number VARCHAR(50) NOT NULL UNIQUE,
    reason VARCHAR(255) NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    tax_refund_amount DECIMAL(12,2) NOT NULL,
    kra_credit_note_number VARCHAR(100) NULL,
    kra_qr_code_string TEXT NULL,
    status ENUM('QUEUED', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'QUEUED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (original_order_id) REFERENCES orders(order_id),
    FOREIGN KEY (original_fiscal_invoice_id) REFERENCES fiscal_invoices(fiscal_invoice_id)
) ENGINE=InnoDB;
