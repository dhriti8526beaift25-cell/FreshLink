-- =============================================================
-- FreshLink – Smart Produce Management System
-- Database Creation Script
-- Database Name: freshlink_db
-- =============================================================

CREATE DATABASE IF NOT EXISTS freshlink_db;
USE freshlink_db;

-- 1. Table: produce_batches
-- Stores agricultural produce batch records
CREATE TABLE IF NOT EXISTS produce_batches (
    batch_id INT PRIMARY KEY AUTO_INCREMENT,
    produce_name VARCHAR(100) NOT NULL,
    quantity DOUBLE NOT NULL,
    harvest_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    price_per_kg DOUBLE NOT NULL
);

-- 2. Table: stock_transactions
-- Tracks stock additions and deductions
CREATE TABLE IF NOT EXISTS stock_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    batch_id INT,
    transaction_type VARCHAR(20) NOT NULL,
    quantity DOUBLE NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES produce_batches(batch_id) ON DELETE CASCADE
);

-- =============================================================
-- Sample Data (Tomatoes, Potatoes, Onions, Apples, Mangoes)
-- Uses INSERT IGNORE so running this script multiple times is safe.
-- =============================================================

INSERT IGNORE INTO produce_batches (batch_id, produce_name, quantity, harvest_date, expiry_date, price_per_kg) VALUES
(101, 'Tomatoes', 50.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 40.0),
(102, 'Tomatoes', 30.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 42.0),
(103, 'Tomatoes', 40.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 45.0),
(104, 'Potatoes', 120.0, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 25.0),
(105, 'Potatoes', 80.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 24.0),
(106, 'Onions', 90.0, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 35.0),
(107, 'Onions', 45.0, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 30.0),
(108, 'Apples', 60.0, DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 120.0),
(109, 'Mangoes', 15.0, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 150.0), -- Expired
(110, 'Mangoes', 50.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 160.0);  -- Fresh

-- Initial stock transactions corresponding to sample batches
INSERT IGNORE INTO stock_transactions (transaction_id, batch_id, transaction_type, quantity, transaction_date) VALUES
(1, 101, 'ADDITION', 50.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 102, 'ADDITION', 30.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 103, 'ADDITION', 40.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 104, 'ADDITION', 120.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 105, 'ADDITION', 80.0, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(6, 106, 'ADDITION', 90.0, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(7, 107, 'ADDITION', 45.0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(8, 108, 'ADDITION', 60.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(9, 109, 'ADDITION', 15.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(10, 110, 'ADDITION', 50.0, DATE_SUB(NOW(), INTERVAL 3 DAY));
