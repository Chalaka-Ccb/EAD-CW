-- ============================================================
--  STARK INDUSTRY - Auto Spare Parts Management System
--  Database Initialization Script
--  Version : 1.0
--  Author  : Stark Industry IT Department
--  Note    : Run this script once to set up the database
-- ============================================================

DROP DATABASE IF EXISTS stark_industry_db;
CREATE DATABASE stark_industry_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE stark_industry_db;

-- ============================================================
--  TABLE : users
--  System login accounts for staff members
-- ============================================================
CREATE TABLE users (
                       user_id   INT AUTO_INCREMENT PRIMARY KEY,
                       username  VARCHAR(50)  NOT NULL UNIQUE,
                       password  VARCHAR(100) NOT NULL,
                       full_name VARCHAR(100) NOT NULL
);

-- ============================================================
--  TABLE : categories
--  Spare part categories available at Stark Industry
-- ============================================================
CREATE TABLE categories (
                            category_id INT AUTO_INCREMENT PRIMARY KEY,
                            name        VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
--  TABLE : customers
--  Both walk-in retail buyers and registered garages
-- ============================================================
CREATE TABLE customers (
                           customer_id   INT AUTO_INCREMENT PRIMARY KEY,
                           name          VARCHAR(100) NOT NULL,
                           phone         VARCHAR(15)  NOT NULL,
                           email         VARCHAR(100),
                           address       VARCHAR(200),
                           customer_type ENUM('Retail','Garage') NOT NULL DEFAULT 'Retail'
);

-- ============================================================
--  TABLE : parts
--  Master list of spare parts stocked by Stark Industry
-- ============================================================
CREATE TABLE parts (
                       part_id       INT AUTO_INCREMENT PRIMARY KEY,
                       part_no       VARCHAR(20)   NOT NULL UNIQUE,
                       name          VARCHAR(100)  NOT NULL,
                       category_id   INT           NOT NULL,
                       brand         VARCHAR(50)   NOT NULL,
                       vehicle_model VARCHAR(100),
                       cost_price    DECIMAL(10,2) NOT NULL CHECK (cost_price >= 0),
                       unit_price    DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
                       stock_qty     INT           NOT NULL CHECK (stock_qty >= 0),
                       reorder_level INT           NOT NULL DEFAULT 10,
                       FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- ============================================================
--  TABLE : sales_invoices
--  Header table for every sales transaction
-- ============================================================
CREATE TABLE sales_invoices (
                                invoice_id     INT AUTO_INCREMENT PRIMARY KEY,
                                customer_id    INT           NOT NULL,
                                user_id        INT           NOT NULL,
                                invoice_date   DATE          NOT NULL,
                                sub_total      DECIMAL(12,2) NOT NULL,
                                discount       DECIMAL(12,2) NOT NULL DEFAULT 0,
                                total_amount   DECIMAL(12,2) NOT NULL,
                                payment_method ENUM('Cash','Card','Credit') NOT NULL,
                                FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                                FOREIGN KEY (user_id)     REFERENCES users(user_id)
);

-- ============================================================
--  TABLE : invoice_items
--  Line items belonging to each invoice
-- ============================================================
CREATE TABLE invoice_items (
                               item_id    INT AUTO_INCREMENT PRIMARY KEY,
                               invoice_id INT           NOT NULL,
                               part_id    INT           NOT NULL,
                               quantity   INT           NOT NULL CHECK (quantity > 0),
                               unit_price DECIMAL(10,2) NOT NULL,
                               line_total DECIMAL(12,2) NOT NULL,
                               FOREIGN KEY (invoice_id) REFERENCES sales_invoices(invoice_id) ON DELETE CASCADE,
                               FOREIGN KEY (part_id)    REFERENCES parts(part_id)
);

-- ============================================================
--  SAMPLE DATA
-- ============================================================

-- ---------- Staff Accounts ----------
INSERT INTO users (username, password, full_name) VALUES
                                                      ('admin',      'admin@123',   'Nimal Rajapaksha'),
                                                      ('kasun.s',    'kasun@456',   'Kasun Silva'),
                                                      ('dilani.m',   'dilani@789',  'Dilani Mendis');

-- ---------- Spare Part Categories ----------
INSERT INTO categories (name) VALUES
                                  ('Brake System'),
                                  ('Engine Components'),
                                  ('Electrical & Battery'),
                                  ('Suspension & Steering'),
                                  ('Filters & Lubricants'),
                                  ('Body & Lighting'),
                                  ('Transmission');

-- ---------- Customers ----------
INSERT INTO customers (name, phone, email, address, customer_type) VALUES
                                                                       ('Walk-in Customer',        '0000000000', NULL,                          'Counter Sale',       'Retail'),
                                                                       ('Kamal Perera',            '0771234567', 'kamal.perera@gmail.com',      'No. 45, Galle Rd, Colombo 05', 'Retail'),
                                                                       ('Lanka Auto Garage',       '0112345678', 'info@lankaautogarage.lk',     'No. 12, High Level Rd, Nugegoda', 'Garage'),
                                                                       ('Silva Motors',            '0812233445', 'silvamotors@gmail.com',       'No. 78, Peradeniya Rd, Kandy', 'Garage'),
                                                                       ('Ranjith Fernando',        '0719876543', 'ranjith.f@yahoo.com',         'No. 23, Main St, Negombo', 'Retail'),
                                                                       ('City Wheels Service Center','0115556677','contact@citywheels.lk',       'No. 5, Baseline Rd, Colombo 09', 'Garage'),
                                                                       ('Tharindu Jayawardena',    '0763344556', 'tharindu.j@gmail.com',        'No. 90, Hospital Rd, Gampaha', 'Retail');

-- ---------- Spare Parts Catalog ----------
INSERT INTO parts (part_no, name, category_id, brand, vehicle_model, cost_price, unit_price, stock_qty, reorder_level) VALUES
                                                                                                                           ('BP-1001', 'Brake Pad Set (Front)',      1, 'Bosch',    'Toyota Axio / Premio',    6500.00,  8500.00,  20,  8),
                                                                                                                           ('BD-1002', 'Brake Disc Rotor',           1, 'Brembo',   'Honda Fit / Vezel',       9000.00, 12500.00,   6,  5),
                                                                                                                           ('BC-1003', 'Brake Caliper Assembly',     1, 'Nissin',   'Toyota Vitz',            12500.00, 16800.00,   5,  4),
                                                                                                                           ('SP-2001', 'Iridium Spark Plug',         2, 'NGK',      'Universal',               1200.00,  1800.00,  40, 16),
                                                                                                                           ('CP-2002', 'Clutch Plate Set',           2, 'Exedy',    'Suzuki Alto / Wagon R',   7500.00, 10500.00,   4,  5),
                                                                                                                           ('TB-2003', 'Timing Belt Kit',            2, 'Gates',    'Mitsubishi Lancer',       5500.00,  7900.00,  10,  5),
                                                                                                                           ('BT-3001', 'Battery 12V 45Ah',           3, 'Amaron',   'Universal',              14500.00, 18500.00,  10,  4),
                                                                                                                           ('BT-3002', 'Battery 12V 65Ah',           3, 'Exide',    'Universal',              19500.00, 24500.00,   6,  3),
                                                                                                                           ('HB-3003', 'Headlamp Bulb H4',           3, 'Philips',  'Universal',                450.00,   850.00,  80, 30),
                                                                                                                           ('SH-4001', 'Shock Absorber (Front)',     4, 'KYB',      'Toyota Vitz / Passo',     8800.00, 12000.00,   8,  6),
                                                                                                                           ('SH-4002', 'Shock Absorber (Rear)',      4, 'Monroe',   'Honda Civic',             9200.00, 12800.00,   5,  5),
                                                                                                                           ('SB-4003', 'Stabilizer Link',            4, '555',      'Toyota Corolla',          1800.00,  2800.00,  15,  8),
                                                                                                                           ('OF-5001', 'Oil Filter',                 5, 'Denso',    'Toyota / Honda',           900.00,  1400.00,  50, 20),
                                                                                                                           ('AF-5002', 'Air Filter',                 5, 'Denso',    'Toyota Axio',             1100.00,  1700.00,   3, 10),
                                                                                                                           ('EO-5003', 'Engine Oil 5W-30 (4L)',      5, 'Castrol',  'Universal',               9500.00, 12500.00,  25, 10),
                                                                                                                           ('AC-5004', 'Air Conditioning Filter',    5, 'Mann',     'Nissan Leaf',              1500.00,  2400.00,  12,  6),
                                                                                                                           ('HL-6001', 'LED Headlight Bulb Pair',    6, 'Osram',    'Universal',               3500.00,  5500.00,  18,  8),
                                                                                                                           ('WM-6002', 'Windshield Wiper Blade Set', 6, 'Valeo',    'Toyota Prius',            2200.00,  3400.00,  14,  6),
                                                                                                                           ('TM-7001', 'Automatic Gear Oil ATF',     7, 'Toyota',   'Universal',               3800.00,  5600.00,  20,  8),
                                                                                                                           ('CV-7002', 'CV Joint Boot Kit',          7, 'GKN',      'Honda Fit',               1600.00,  2600.00,  10,  5);

-- ---------- Sales Invoices ----------
INSERT INTO sales_invoices (customer_id, user_id, invoice_date, sub_total, discount, total_amount, payment_method) VALUES
                                                                                                                       (2, 1, CURDATE(),                                      11300.00,    0.00, 11300.00, 'Cash'),
                                                                                                                       (3, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY),            40700.00, 2035.00, 38665.00, 'Credit'),
                                                                                                                       (4, 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY),           51000.00, 2550.00, 48450.00, 'Card'),
                                                                                                                       (5, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY),           15900.00,    0.00, 15900.00, 'Cash'),
                                                                                                                       (6, 2, DATE_SUB(CURDATE(), INTERVAL 25 DAY),           74200.00, 3710.00, 70490.00, 'Credit'),
                                                                                                                       (7, 1, DATE_SUB(CURDATE(), INTERVAL 40 DAY),           28600.00,  500.00, 28100.00, 'Card');

-- ---------- Invoice Line Items ----------
INSERT INTO invoice_items (invoice_id, part_id, quantity, unit_price, line_total) VALUES
                                                                                      (1,  1,  1,  8500.00,  8500.00),   -- Brake Pad Set
                                                                                      (1, 13,  2,  1400.00,  2800.00),   -- Oil Filters x2

                                                                                      (2,  4,  4,  1800.00,  7200.00),   -- Spark Plugs x4
                                                                                      (2,  9, 10,   850.00,  8500.00),   -- Headlamp Bulbs x10
                                                                                      (2, 15,  2, 12500.00, 25000.00),   -- Engine Oil x2

                                                                                      (3,  7,  1, 18500.00, 18500.00),   -- Battery 45Ah
                                                                                      (3, 10,  2, 12000.00, 24000.00),   -- Front Shock Absorbers
                                                                                      (3, 14,  5,  1700.00,  8500.00),   -- Air Filters x5

                                                                                      (4,  2,  1, 12500.00, 12500.00),   -- Brake Disc
                                                                                      (4, 18,  1,  3400.00,  3400.00),   -- Wiper Blade Set

                                                                                      (5,  8,  2, 24500.00, 49000.00),   -- Battery 65Ah x2
                                                                                      (5,  3,  1, 16800.00, 16800.00),   -- Brake Caliper
                                                                                      (5, 17,  2,  5500.00, 11000.00),   -- LED Headlight Pairs

                                                                                      (6, 16,  3,  2400.00,   7200.00),  -- AC Filters x3
                                                                                      (6, 19,  4,  5600.00,  22400.00),  -- ATF Oil x4
                                                                                      (6, 12,  5,  2800.00,  14000.00);  -- Stabilizer Links x5

-- ============================================================
--  END OF SCRIPT
--  Stark Industry - Drive Safe, Drive Strong.
-- ============================================================                                                                  (4, 9, 2,  1700.00,  3400.00);