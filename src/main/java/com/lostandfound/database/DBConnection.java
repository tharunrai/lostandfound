package com.lostandfound.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/lost_and_found?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "admin";
    private static final String PASSWORD = "rai@admin"; // Default local dev password

    static {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE ITEM ADD COLUMN image_path VARCHAR(255) NULL");
            
            // Auto-migrate trigger 1: Update item status when claim is approved
            try {
                stmt.executeUpdate("DROP TRIGGER IF EXISTS after_claim_approved");
                stmt.executeUpdate(
                    "CREATE TRIGGER after_claim_approved " +
                    "AFTER UPDATE ON CLAIM " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "    IF NEW.claim_status = 'approved' AND (OLD.claim_status IS NULL OR OLD.claim_status <> 'approved') THEN " +
                    "        UPDATE ITEM " +
                    "        SET status = 'claimed' " +
                    "        WHERE item_id = NEW.item_id; " +
                    "    END IF; " +
                    "END"
                );
            } catch (SQLException e) {
                System.err.println("Auto-migration warning (trigger 1): " + e.getMessage());
            }

            // Auto-migrate trigger 2: Reject pending claims when item status is set to claimed
            try {
                stmt.executeUpdate("DROP TRIGGER IF EXISTS after_item_status_claimed");
                stmt.executeUpdate(
                    "CREATE TRIGGER after_item_status_claimed " +
                    "AFTER UPDATE ON ITEM " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "    IF NEW.status = 'claimed' AND (OLD.status IS NULL OR OLD.status <> 'claimed') THEN " +
                    "        UPDATE CLAIM " +
                    "        SET claim_status = 'rejected' " +
                    "        WHERE item_id = NEW.item_id AND claim_status = 'pending'; " +
                    "    END IF; " +
                    "END"
                );
            } catch (SQLException e) {
                System.err.println("Auto-migration warning (trigger 2): " + e.getMessage());
            }

            // Insert Category 'Other' if not present
            try {
                stmt.executeUpdate("INSERT IGNORE INTO CATEGORY (category_id, category_name) VALUES (6, 'Other')");
            } catch (SQLException e) {
                System.err.println("Auto-migration warning (Other Category): " + e.getMessage());
            }
        } catch (SQLException e) {
            // Ignore error 1060 (Duplicate column name) because it means the column already exists
            if (e.getErrorCode() != 1060) {
                System.err.println("Auto-migration warning: " + e.getMessage());
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
