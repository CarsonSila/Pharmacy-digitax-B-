USE pharmacore_db;

-- Insert Seed Products
INSERT INTO products (product_id, sku, barcode, brand_name, generic_name, category_id, base_unit_id, tax_class_id, hs_code, prescription_required, cost_price, selling_price, reorder_level) VALUES
(1, 'MED-AMOX-500', '890123456001', 'Amoxil 500mg', 'Amoxicillin Trihydrate', 1, 1, 1, '3004.10.00', 1, 15.00, 25.00, 50),
(2, 'MED-PARA-500', '890123456002', 'Panadol Extra', 'Paracetamol / Caffeine', 2, 1, 1, '3004.90.00', 0, 8.00, 15.00, 100),
(3, 'BEAU-SUN-50',  '890123456003', 'Nivea Sun Protect SPF 50', 'Sunscreen Lotion 100ml', 4, 4, 3, '3304.99.00', 0, 1100.00, 1650.00, 10),
(4, 'DEV-SYR-5ML',  '890123456004', 'BD Discardit 5ml Syringe', 'Hypodermic Syringe', 5, 1, 2, '9018.31.00', 0, 12.00, 20.00, 50)
ON DUPLICATE KEY UPDATE brand_name=VALUES(brand_name);

-- Unit conversions (e.g. Panadol: 1 Blister = 10 tablets, 1 Box = 100 tablets)
INSERT INTO product_unit_conversions (product_id, unit_id, conversion_factor, selling_price, barcode) VALUES
(1, 2, 10, 230.00, '890123456001-STRIP'), -- 1 Strip of Amoxil (10 caps)
(1, 3, 100, 2200.00, '890123456001-BOX'),  -- 1 Box of Amoxil (100 caps)
(2, 2, 10, 140.00, '890123456002-STRIP'),  -- 1 Strip of Panadol (10 tabs)
(2, 3, 100, 1300.00, '890123456002-BOX')   -- 1 Box of Panadol (100 tabs)
ON DUPLICATE KEY UPDATE selling_price=VALUES(selling_price);

-- Insert Product Batches with FEFO expiry dates
INSERT INTO product_batches (batch_id, product_id, batch_number, manufacturing_date, expiry_date, cost_price, quantity_available, supplier_name) VALUES
(1, 1, 'BAT-AMX-2026A', '2026-01-10', '2027-12-31', 15.00, 500, 'GlaxoSmithKline Kenya'),
(2, 1, 'BAT-AMX-2025B', '2025-06-01', '2026-11-30', 14.50, 200, 'GlaxoSmithKline Kenya'), -- Earlier expiry! Should be picked first by FEFO
(3, 2, 'BAT-PAN-2026C', '2026-03-15', '2028-03-31', 8.00, 1000, 'Haleon East Africa'),
(4, 3, 'BAT-NIV-2026D', '2026-02-01', '2028-02-01', 1100.00, 25, 'Beiersdorf E.A.'),
(5, 4, 'BAT-SYR-2026E', '2026-01-01', '2029-01-01', 12.00, 300, 'Becton Dickinson')
ON DUPLICATE KEY UPDATE quantity_available=VALUES(quantity_available);

-- Insert Sample Customer
INSERT INTO customers (customer_id, full_name, phone, email, kra_pin) VALUES
(1, 'Walk-in Retail Customer', '0700000000', 'walkin@pharmacore.com', NULL),
(2, 'Aga Khan University Hospital', '0711000111', 'procurement@aku.edu', 'P051234567Z')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);
