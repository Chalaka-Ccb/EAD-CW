-- SAFE MIGRATION: run once in phpMyAdmin after selecting your existing database.
-- Does NOT drop or recreate any existing tables or records.
USE stark_industry_db;
CREATE TABLE IF NOT EXISTS sales_returns (
 return_id INT AUTO_INCREMENT PRIMARY KEY,
 invoice_id INT NOT NULL,
 item_id INT NOT NULL,
 part_id INT NOT NULL,
 quantity INT NOT NULL,
 refund_amount DECIMAL(12,2) NOT NULL,
 reason VARCHAR(255) NOT NULL,
 returned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 processed_by INT NOT NULL,
 FOREIGN KEY (invoice_id) REFERENCES sales_invoices(invoice_id),
 FOREIGN KEY (item_id) REFERENCES invoice_items(item_id),
 FOREIGN KEY (part_id) REFERENCES parts(part_id),
 FOREIGN KEY (processed_by) REFERENCES users(user_id),
 CHECK (quantity > 0), CHECK (refund_amount >= 0)
);
CREATE INDEX idx_sales_returns_invoice ON sales_returns(invoice_id);
