CREATE DATABASE IF NOT EXISTS lost_and_found;
USE lost_and_found;

CREATE TABLE IF NOT EXISTS USER (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    role ENUM('admin', 'user') DEFAULT 'user',
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS CATEGORY (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS LOCATION (
    location_id INT PRIMARY KEY AUTO_INCREMENT,
    location_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS ITEM (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status ENUM('lost', 'found', 'claimed') NOT NULL,
    reported_date DATE NOT NULL,
    user_id INT,
    category_id INT,
    location_id INT,
    image_path VARCHAR(255) NULL,
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES LOCATION(location_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS CLAIM (
    claim_id INT PRIMARY KEY AUTO_INCREMENT,
    item_id INT,
    claimer_id INT,
    claim_status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    claim_date DATE NOT NULL,
    FOREIGN KEY (item_id) REFERENCES ITEM(item_id) ON DELETE CASCADE,
    FOREIGN KEY (claimer_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_item_claimer UNIQUE (item_id, claimer_id)
);

-- Table for users reporting they found a match for a lost item
CREATE TABLE IF NOT EXISTS MATCH_REPORT (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    item_id INT NOT NULL,
    reporter_id INT NOT NULL,
    note TEXT,
    report_date DATE NOT NULL,
    FOREIGN KEY (item_id) REFERENCES ITEM(item_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_match_reporter UNIQUE (item_id, reporter_id)
);

-- ==========================================
-- TRIGGERS
-- ==========================================

-- Trigger 1: Auto-mark item as claimed when a claim is approved
DROP TRIGGER IF EXISTS after_claim_approved;
DELIMITER //
CREATE TRIGGER after_claim_approved
AFTER UPDATE ON CLAIM
FOR EACH ROW
BEGIN
    IF NEW.claim_status = 'approved' AND (OLD.claim_status IS NULL OR OLD.claim_status <> 'approved') THEN
        UPDATE ITEM
        SET status = 'claimed'
        WHERE item_id = NEW.item_id;
    END IF;
END //
DELIMITER ;

-- Trigger 2: Auto-reject other pending claims when an item's status changes to claimed
DROP TRIGGER IF EXISTS after_item_status_claimed;
DELIMITER //
CREATE TRIGGER after_item_status_claimed
AFTER UPDATE ON ITEM
FOR EACH ROW
BEGIN
    IF NEW.status = 'claimed' AND (OLD.status IS NULL OR OLD.status <> 'claimed') THEN
        UPDATE CLAIM
        SET claim_status = 'rejected'
        WHERE item_id = NEW.item_id AND claim_status = 'pending';
    END IF;
END //
DELIMITER ;

-- Categories
INSERT IGNORE INTO CATEGORY (category_id, category_name) VALUES
(1, 'Electronics'),
(2, 'Bags'),
(3, 'Keys'),
(4, 'Clothing'),
(5, 'Documents'),
(6, 'Other');

-- Locations
INSERT IGNORE INTO LOCATION (location_id, location_name) VALUES
(1, 'Library'),
(2, 'Canteen'),
(3, 'Main Gate'),
(4, 'Parking Lot'),
(5, 'Classroom');

-- Users with BCrypt hashed passwords:
-- admin@email.com -> admin123
-- rahul@email.com -> rahul123
-- priya@email.com -> priya123
INSERT IGNORE INTO USER (user_id, name, email, phone, role, password) VALUES
(1, 'Admin User', 'admin@email.com', '9000000000', 'admin', '$2a$10$IbFT5Nsl7rdRfBieyI2O7.SaJ79Da23H6i2Cp6zispBtKTkPzs15u'),
(2, 'Rahul Kumar', 'rahul@email.com', '9876543210', 'user', '$2a$10$aMjqMmjRLrBjJppmNTNBdOZTe.4PfCWslnpzu4ZDebRUFCzDBXt0K'),
(3, 'Priya Singh', 'priya@email.com', '9123456780', 'user', '$2a$10$JuzxBe.PIXy7sYI9DEqvUO5r8eKQOpBudXXBfTks7V.H9Bad3fyVK');

-- Items
INSERT IGNORE INTO ITEM (item_id, title, description, status, reported_date, user_id, category_id, location_id) VALUES
(1, 'Black Backpack', 'Nike bag with laptop inside', 'lost', CURDATE(), 2, 2, 1),
(2, 'iPhone 13', 'Black colour, cracked screen', 'found', CURDATE(), 3, 1, 3),
(3, 'Car Keys', 'Honda car keys with red keychain', 'lost', CURDATE(), 2, 3, 4);

-- Claims
INSERT IGNORE INTO CLAIM (claim_id, item_id, claimer_id, claim_status, claim_date) VALUES
(1, 2, 2, 'pending', CURDATE());