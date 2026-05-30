package com.lostandfound.dao;

import com.lostandfound.database.DBConnection;
import com.lostandfound.models.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimDAO {

    public boolean addClaim(Claim claim) {
        String query = "INSERT INTO CLAIM (item_id, claimer_id, claim_status, claim_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, claim.getItemId());
            stmt.setInt(2, claim.getClaimerId());
            stmt.setString(3, claim.getClaimStatus());
            stmt.setDate(4, claim.getClaimDate());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasUserClaimedItem(int userId, int itemId) {
        String query = "SELECT COUNT(*) FROM CLAIM WHERE claimer_id = ? AND item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Claim> getClaimsByUser(int userId) {
        // Exclude 'lost' items — CLAIM rows on lost items are match reports, not real claims
        String query = "SELECT c.*, i.title as item_title " +
                       "FROM CLAIM c " +
                       "JOIN ITEM i ON c.item_id = i.item_id " +
                       "WHERE c.claimer_id = ? AND i.status != 'lost' " +
                       "ORDER BY c.claim_date DESC";
        List<Claim> claims = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                claims.add(mapResultSetToClaim(rs, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claims;
    }

    public List<Claim> getAllClaims() {
        // Exclude 'lost' items — CLAIM rows on lost items are match reports, not real claims
        String query = "SELECT c.*, i.title as item_title, u.name as claimer_name " +
                       "FROM CLAIM c " +
                       "JOIN ITEM i ON c.item_id = i.item_id " +
                       "JOIN USER u ON c.claimer_id = u.user_id " +
                       "WHERE i.status != 'lost' " +
                       "ORDER BY c.claim_date DESC";
        List<Claim> claims = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                claims.add(mapResultSetToClaim(rs, true));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claims;
    }

    public boolean updateClaimStatus(int claimId, String status) {
        String query = "UPDATE CLAIM SET claim_status = ? WHERE claim_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, claimId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Claim mapResultSetToClaim(ResultSet rs, boolean includeClaimerName) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getInt("claim_id"));
        claim.setItemId(rs.getInt("item_id"));
        claim.setClaimerId(rs.getInt("claimer_id"));
        claim.setClaimStatus(rs.getString("claim_status"));
        claim.setClaimDate(rs.getDate("claim_date"));
        claim.setItemTitle(rs.getString("item_title"));
        
        if (includeClaimerName) {
            claim.setClaimerName(rs.getString("claimer_name"));
        }
        
        return claim;
    }
}
