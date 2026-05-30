package com.lostandfound.dao;

import com.lostandfound.database.DBConnection;
import com.lostandfound.models.MatchReport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MatchReportDAO — stores "Found a Match" reports using the existing CLAIM table.
 * Lost items can receive claims (match reports) just like found items receive claims.
 * No additional database table is required; this preserves the existing schema.
 */
public class MatchReportDAO {

    /** Submit a match report for a lost item (stored as a CLAIM record). */
    public boolean addMatchReport(MatchReport report) {
        String query = "INSERT INTO CLAIM (item_id, claimer_id, claim_status, claim_date) VALUES (?, ?, 'pending', ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, report.getItemId());
            stmt.setInt(2, report.getReporterId());
            stmt.setDate(3, report.getReportDate());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Duplicate (unique_item_claimer) → already reported
            System.err.println("[MatchReportDAO] addMatchReport: " + e.getMessage());
        }
        return false;
    }

    /** Check if a user has already submitted a match report for this item. */
    public boolean hasUserReportedMatch(int userId, int itemId) {
        String query = "SELECT COUNT(*) FROM CLAIM WHERE claimer_id = ? AND item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Get the total number of match reports (claims) for a given item. */
    public int getMatchReportCount(int itemId) {
        String query = "SELECT COUNT(*) FROM CLAIM WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Get all match reports for a given item (reads from CLAIM + USER join). */
    public List<MatchReport> getMatchReportsByItem(int itemId) {
        String query = "SELECT c.claim_id, c.item_id, c.claimer_id, c.claim_date, " +
                       "u.name as reporter_name, i.title as item_title " +
                       "FROM CLAIM c " +
                       "JOIN USER u ON c.claimer_id = u.user_id " +
                       "JOIN ITEM i ON c.item_id = i.item_id " +
                       "WHERE c.item_id = ? ORDER BY c.claim_date DESC";
        List<MatchReport> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MatchReport r = new MatchReport();
                r.setReportId(rs.getInt("claim_id"));
                r.setItemId(rs.getInt("item_id"));
                r.setReporterId(rs.getInt("claimer_id"));
                r.setReportDate(rs.getDate("claim_date"));
                r.setReporterName(rs.getString("reporter_name"));
                r.setItemTitle(rs.getString("item_title"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
