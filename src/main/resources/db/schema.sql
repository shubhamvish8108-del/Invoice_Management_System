-- Invoice Management System Database Schema
-- Run this SQL script in MySQL to create the required tables

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS invoice_db;
USE invoice_db;

-- ============================================
-- CHAIN TABLE (for GST information)
-- ============================================
CREATE TABLE IF NOT EXISTS chain (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chain_name VARCHAR(100) NOT NULL,
    gst_number VARCHAR(20),
    address VARCHAR(200),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- ESTIMATE TABLE (linked to invoice)
-- ============================================
CREATE TABLE IF NOT EXISTS estimate (
    id INT PRIMARY KEY AUTO_INCREMENT,
    estimate_no VARCHAR(20) UNIQUE NOT NULL,
    chain_id INT,
    service_details VARCHAR(500),
    qty INT,
    cost_per_qty DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (chain_id) REFERENCES chain(id)
);

-- ============================================
-- INVOICE TABLE (Main table for invoices)
-- ============================================
CREATE TABLE IF NOT EXISTS invoice (
    id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_no VARCHAR(10) UNIQUE NOT NULL,
    estimated_id INT NOT NULL,
    chain_id INT NOT NULL,
    service_details VARCHAR(500),
    qty INT,
    cost_per_qty DECIMAL(10,2),
    amount_payable DECIMAL(10,2),
    balance DECIMAL(10,2) DEFAULT 0,
    date_of_payment DATETIME,
    date_of_service DATE,
    delivery_details VARCHAR(200),
    email_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'GENERATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (estimated_id) REFERENCES estimate(id),
    FOREIGN KEY (chain_id) REFERENCES chain(id)
);

-- ============================================
-- INVOICE_SEQUENCE TABLE (for generating invoice numbers)
-- ============================================
CREATE TABLE IF NOT EXISTS invoice_sequence (
    id INT PRIMARY KEY AUTO_INCREMENT,
    last_invoice_number INT DEFAULT 0
);

-- Initialize sequence
INSERT INTO invoice_sequence (last_invoice_number) VALUES (0);

-- ============================================
-- INSERT SAMPLE DATA FOR TESTING
-- ============================================

-- Insert sample chains
INSERT INTO chain (chain_name, gst_number, address, contact_email, contact_phone) VALUES
('Code-B Tech Solutions', '27AABCU9603R1ZM', '123 Tech Park, Mumbai, MH', 'billing@codebtech.com', '+91 9876543210'),
('ABC Corporation', '19AABCU9603R1ZM', '456 Business Center, Chennai, TN', 'accounts@abccorp.com', '+91 8765432109'),
('XYZ Industries', '29AABCU9603R1ZM', '789 Industrial Area, Delhi', 'finance@xyzind.com', '+91 7654321098');

-- Insert sample estimates
INSERT INTO estimate (estimate_no, chain_id, service_details, qty, cost_per_qty, total_amount, status) VALUES
('EST-0001', 1, 'Software Development Services', 100, 500.00, 50000.00, 'APPROVED'),
('EST-0002', 2, 'IT Consulting Services', 50, 1000.00, 50000.00, 'APPROVED'),
('EST-0003', 3, 'Maintenance Services', 200, 250.00, 50000.00, 'PENDING');

-- ============================================
-- INDEXES FOR BETTER PERFORMANCE
-- ============================================
CREATE INDEX idx_invoice_chain_id ON invoice(chain_id);
CREATE INDEX idx_invoice_estimated_id ON invoice(estimated_id);
CREATE INDEX idx_invoice_invoice_no ON invoice(invoice_no);
CREATE INDEX idx_estimate_chain_id ON estimate(chain_id);

-- ============================================
-- VIEW INVOICE DETAILS (with chain info)
-- ============================================
CREATE OR REPLACE VIEW invoice_details_view AS
SELECT
    i.id,
    i.invoice_no,
    i.estimated_id,
    i.chain_id,
    c.chain_name,
    c.gst_number,
    c.address,
    c.contact_email AS chain_email,
    i.service_details,
    i.qty,
    i.cost_per_qty,
    i.amount_payable,
    i.balance,
    i.date_of_payment,
    i.date_of_service,
    i.delivery_details,
    i.email_id,
    i.status,
    i.created_at,
    i.updated_at
FROM invoice i
JOIN chain c ON i.chain_id = c.id;
