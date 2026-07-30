-- Sample Products and Inventory Data for Inventory Service
-- This file contains sample product and inventory data for development and testing

-- Insert sample products
INSERT INTO products (name, description, price, sku, active) VALUES
('Laptop Pro 15', 'High-performance laptop with 15-inch display, Intel i7, 16GB RAM', 1299.99, 'LAPTOP-PRO-15', TRUE),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 'MOUSE-WIRELESS-01', TRUE),
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 89.99, 'KEYBOARD-MECH-RGB', TRUE),
('USB-C Hub', '7-in-1 USB-C hub with HDMI, USB 3.0, SD card reader', 49.99, 'HUB-USBC-7IN1', TRUE),
('Webcam HD 1080p', 'Full HD webcam with built-in microphone', 79.99, 'WEBCAM-HD-1080', TRUE),
('Monitor 27"', '27-inch 4K monitor with IPS panel', 399.99, 'MONITOR-27-4K', TRUE),
('Headphones Bluetooth', 'Noise-canceling Bluetooth headphones', 149.99, 'HEADPHONES-BT-NC', TRUE),
('External SSD 1TB', 'Portable external SSD 1TB with USB 3.1', 119.99, 'SSD-EXT-1TB', TRUE),
('Laptop Stand', 'Adjustable aluminum laptop stand', 39.99, 'STAND-LAPTOP-ADJ', TRUE),
('Cable Organizer', 'Desktop cable management organizer', 14.99, 'ORGANIZER-CABLE', TRUE),
('Desk Lamp LED', 'Adjustable LED desk lamp with USB charging', 34.99, 'LAMP-DESK-LED', TRUE),
('Mouse Pad XL', 'Extra large gaming mouse pad', 19.99, 'PAD-MOUSE-XL', TRUE),
('Phone Holder', 'Adjustable phone holder for desk', 24.99, 'HOLDER-PHONE-DESK', TRUE),
('Webcam Cover', 'Privacy webcam cover slide', 4.99, 'COVER-WEBCAM', TRUE),
('Laptop Sleeve 15"', 'Protective laptop sleeve for 15-inch laptops', 22.99, 'SLEEVE-LAPTOP-15', TRUE);

-- Initialize inventory for all products
INSERT INTO inventory (product_id, available_quantity, reserved_quantity, last_updated) VALUES
(1, 50, 0, CURRENT_TIMESTAMP),   -- Laptop Pro 15
(2, 200, 0, CURRENT_TIMESTAMP),  -- Wireless Mouse
(3, 100, 0, CURRENT_TIMESTAMP),  -- Mechanical Keyboard
(4, 150, 0, CURRENT_TIMESTAMP),  -- USB-C Hub
(5, 75, 0, CURRENT_TIMESTAMP),   -- Webcam HD 1080p
(6, 30, 0, CURRENT_TIMESTAMP),   -- Monitor 27"
(7, 80, 0, CURRENT_TIMESTAMP),   -- Headphones Bluetooth
(8, 60, 0, CURRENT_TIMESTAMP),   -- External SSD 1TB
(9, 120, 0, CURRENT_TIMESTAMP),  -- Laptop Stand
(10, 300, 0, CURRENT_TIMESTAMP), -- Cable Organizer
(11, 90, 0, CURRENT_TIMESTAMP),  -- Desk Lamp LED
(12, 250, 0, CURRENT_TIMESTAMP), -- Mouse Pad XL
(13, 180, 0, CURRENT_TIMESTAMP), -- Phone Holder
(14, 500, 0, CURRENT_TIMESTAMP), -- Webcam Cover
(15, 110, 0, CURRENT_TIMESTAMP); -- Laptop Sleeve 15"

-- Add initial inventory history entries
INSERT INTO inventory_history (product_id, action, quantity, timestamp) VALUES
(1, 'ADD', 50, CURRENT_TIMESTAMP),
(2, 'ADD', 200, CURRENT_TIMESTAMP),
(3, 'ADD', 100, CURRENT_TIMESTAMP),
(4, 'ADD', 150, CURRENT_TIMESTAMP),
(5, 'ADD', 75, CURRENT_TIMESTAMP),
(6, 'ADD', 30, CURRENT_TIMESTAMP),
(7, 'ADD', 80, CURRENT_TIMESTAMP),
(8, 'ADD', 60, CURRENT_TIMESTAMP),
(9, 'ADD', 120, CURRENT_TIMESTAMP),
(10, 'ADD', 300, CURRENT_TIMESTAMP),
(11, 'ADD', 90, CURRENT_TIMESTAMP),
(12, 'ADD', 250, CURRENT_TIMESTAMP),
(13, 'ADD', 180, CURRENT_TIMESTAMP),
(14, 'ADD', 500, CURRENT_TIMESTAMP),
(15, 'ADD', 110, CURRENT_TIMESTAMP);

-- Verify data
SELECT COUNT(*) AS total_products FROM products;
SELECT COUNT(*) AS total_inventory_records FROM inventory;
SELECT COUNT(*) AS total_history_entries FROM inventory_history;

-- Show products with inventory
SELECT
    p.id,
    p.name,
    p.sku,
    p.price,
    i.available_quantity,
    i.reserved_quantity,
    (i.available_quantity + i.reserved_quantity) AS total_quantity
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id
ORDER BY p.id;
