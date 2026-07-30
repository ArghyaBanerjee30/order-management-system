-- Sample Customers Data for Order Service
-- This file contains sample customer data for development and testing

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone, created_at) VALUES
('John', 'Doe', 'john.doe@example.com', '+1-555-123-4567', CURRENT_TIMESTAMP),
('Jane', 'Smith', 'jane.smith@example.com', '+1-555-234-5678', CURRENT_TIMESTAMP),
('Michael', 'Johnson', 'michael.j@example.com', '+1-555-345-6789', CURRENT_TIMESTAMP),
('Emily', 'Williams', 'emily.w@example.com', '+1-555-456-7890', CURRENT_TIMESTAMP),
('David', 'Brown', 'david.brown@example.com', '+1-555-567-8901', CURRENT_TIMESTAMP),
('Sarah', 'Davis', 'sarah.davis@example.com', '+1-555-678-9012', CURRENT_TIMESTAMP),
('James', 'Miller', 'james.miller@example.com', '+1-555-789-0123', CURRENT_TIMESTAMP),
('Lisa', 'Wilson', 'lisa.wilson@example.com', '+1-555-890-1234', CURRENT_TIMESTAMP),
('Robert', 'Moore', 'robert.moore@example.com', '+1-555-901-2345', CURRENT_TIMESTAMP),
('Jennifer', 'Taylor', 'jennifer.taylor@example.com', '+1-555-012-3456', CURRENT_TIMESTAMP);

-- Verify data
SELECT COUNT(*) AS total_customers FROM customers;
SELECT * FROM customers ORDER BY id;
