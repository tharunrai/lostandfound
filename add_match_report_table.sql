-- Run this script to add the MATCH_REPORT table to your existing database
-- Execute in your MySQL client: SOURCE path/to/add_match_report.sql;
-- OR paste it directly in MySQL Workbench / phpMyAdmin

USE lost_and_found;

CREATE TABLE IF NOT EXISTS MATCH_REPORT (
    report_id   INT PRIMARY KEY AUTO_INCREMENT,
    item_id     INT NOT NULL,
    reporter_id INT NOT NULL,
    note        TEXT,
    report_date DATE NOT NULL,
    FOREIGN KEY (item_id)     REFERENCES ITEM(item_id)   ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES USER(user_id)   ON DELETE CASCADE,
    CONSTRAINT unique_match_reporter UNIQUE (item_id, reporter_id)
);

SELECT 'MATCH_REPORT table created successfully.' AS status;
