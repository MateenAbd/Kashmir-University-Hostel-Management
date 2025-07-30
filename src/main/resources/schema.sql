-- Database schema for Hostel Management System

CREATE DATABASE IF NOT EXISTS hostel_management;
USE hostel_management;

-- Users table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'WARDEN', 'STUDENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- System settings table
CREATE TABLE system_settings (
    setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

-- Form numbers table
CREATE TABLE form_numbers (
    form_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_number VARCHAR(50) UNIQUE NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    admin_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(user_id)
);

-- Registration requests table
CREATE TABLE registration_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_id BIGINT,
    email VARCHAR(255) NOT NULL,
    enrollment_no VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    department VARCHAR(100) NOT NULL,
    batch VARCHAR(20) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    district VARCHAR(100) NOT NULL,
    tehsil VARCHAR(100) NOT NULL,
    guardian_phone VARCHAR(15) NOT NULL,
    photo_url VARCHAR(500),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    comments TEXT,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (form_id) REFERENCES form_numbers(form_id),
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);

-- Students table
CREATE TABLE students (
    student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    enrollment_no VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    department VARCHAR(100) NOT NULL,
    batch VARCHAR(20) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    district VARCHAR(100) NOT NULL,
    tehsil VARCHAR(100) NOT NULL,
    guardian_phone VARCHAR(15) NOT NULL,
    photo_url VARCHAR(500),
    is_monitor BOOLEAN DEFAULT FALSE,
    current_balance DECIMAL(10,2) DEFAULT 0.00 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Attendance table
CREATE TABLE attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT') DEFAULT 'PRESENT',
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_student_date (student_id, date),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

-- Absence requests table
CREATE TABLE absence_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    request_date DATE NOT NULL,
    absence_date DATE NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by BIGINT,
    comments TEXT,
    submitted_at TIMESTAMP NULL,
    approved_at TIMESTAMP NULL,
    is_late_request BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

-- Monthly expenses table
CREATE TABLE monthly_expenses (
    expense_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    month_year VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    total_amount DECIMAL(10,2) NOT NULL,
    entered_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_month_year (month_year),
    FOREIGN KEY (entered_by) REFERENCES users(user_id)
);

-- Bills table
CREATE TABLE bills (
    bill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    month_year VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    amount_due DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) DEFAULT 0.00 NOT NULL,
    present_days INT NOT NULL,
    total_days INT NOT NULL,
    status ENUM('PENDING', 'PARTIALLY_PAID', 'FULLY_PAID') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_student_month (student_id, month_year),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- Payments table
CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method ENUM('CASH', 'ONLINE') NOT NULL,
    transaction_id VARCHAR(100),
    recorded_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(user_id)
);

-- Deletion requests table
CREATE TABLE deletion_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    reviewed_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES users(user_id),
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);

-- Indexes for better performance
CREATE INDEX idx_attendance_student_date ON attendance(student_id, date);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_bills_student_month ON bills(student_id, month_year);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_absence_requests_student ON absence_requests(student_id);
CREATE INDEX idx_absence_requests_status ON absence_requests(status);
CREATE INDEX idx_registration_requests_status ON registration_requests(status);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);

-- Insert default admin user
INSERT INTO users (email, password_hash, role) VALUES
('admin@hostel.com', '$2a$10$I0anM/bxPeYUBkGKbvC74OFf.3EHZQ7m/kCMmTk4WH5WtECXVO7d', 'ADMIN'); //admin123

-- Insert default warden user
INSERT INTO users (email, password_hash, role) VALUES
('warden@hostel.com', '$2a$12$S3eIyEEbyx.uWTj4VCBfvOGN6i1JXZ0SXZ0vjl0ksfwUw.XUh0hhO', 'WARDEN'); //warden123

-- Insert default system setting for absence request cutoff time
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('ABSENCE_REQUEST_CUTOFF_TIME', '11:00', 'Cutoff time for early vs late absence requests (HH:mm format)');


-- Insert some sample form numbers for testing
INSERT IGNORE INTO form_numbers (form_number, is_used, admin_id, created_at) VALUES
('HM2024001', FALSE, 1, NOW()),
('HM2024002', FALSE, 1, NOW()),
('HM2024003', FALSE, 1, NOW()),
('HM2024004', FALSE, 1, NOW()),
('HM2024005', FALSE, 1, NOW());
