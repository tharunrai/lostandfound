package com.lostandfound.dao;

import com.lostandfound.database.DBConnection;
import com.lostandfound.models.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public boolean addItem(Item item) {
        String query = "INSERT INTO ITEM (title, description, status, reported_date, user_id, category_id, location_id, image_path) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, item.getTitle());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getStatus());
            stmt.setDate(4, item.getReportedDate());
            stmt.setInt(5, item.getUserId());
            stmt.setInt(6, item.getCategoryId());
            stmt.setInt(7, item.getLocationId());
            stmt.setString(8, item.getImagePath());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Item> getAllItems() {
        return fetchItems("SELECT i.*, u.name as reporter_name, c.category_name, l.location_name " +
                          "FROM ITEM i " +
                          "LEFT JOIN USER u ON i.user_id = u.user_id " +
                          "LEFT JOIN CATEGORY c ON i.category_id = c.category_id " +
                          "LEFT JOIN LOCATION l ON i.location_id = l.location_id " +
                          "ORDER BY i.reported_date DESC");
    }

    public Item getItemById(int itemId) {
        String query = "SELECT i.*, u.name as reporter_name, c.category_name, l.location_name " +
                       "FROM ITEM i " +
                       "LEFT JOIN USER u ON i.user_id = u.user_id " +
                       "LEFT JOIN CATEGORY c ON i.category_id = c.category_id " +
                       "LEFT JOIN LOCATION l ON i.location_id = l.location_id " +
                       "WHERE i.item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Item> searchItems(String keyword, String category, String status) {
        StringBuilder query = new StringBuilder(
            "SELECT i.*, u.name as reporter_name, c.category_name, l.location_name " +
            "FROM ITEM i " +
            "LEFT JOIN USER u ON i.user_id = u.user_id " +
            "LEFT JOIN CATEGORY c ON i.category_id = c.category_id " +
            "LEFT JOIN LOCATION l ON i.location_id = l.location_id " +
            "WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (i.title LIKE ? OR i.description LIKE ?) ");
        }
        if (category != null && !category.trim().isEmpty() && !category.equals("All")) {
            query.append(" AND c.category_name = ? ");
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("All")) {
            query.append(" AND i.status = ? ");
        }

        query.append(" ORDER BY i.reported_date DESC");

        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + keyword + "%");
                stmt.setString(paramIndex++, "%" + keyword + "%");
            }
            if (category != null && !category.trim().isEmpty() && !category.equals("All")) {
                stmt.setString(paramIndex++, category);
            }
            if (status != null && !status.trim().isEmpty() && !status.equals("All")) {
                stmt.setString(paramIndex++, status);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /** Returns ALL items (any status) with a comma-separated list of claimer names attached. Used by AdminPanel. */
    public List<Item> getAllItemsForAdmin() {
        String query = "SELECT i.*, u.name as reporter_name, c.category_name, l.location_name, " +
                       "GROUP_CONCAT(cu.name ORDER BY cu.name SEPARATOR ', ') as claimer_names " +
                       "FROM ITEM i " +
                       "LEFT JOIN USER u  ON i.user_id      = u.user_id " +
                       "LEFT JOIN CATEGORY c ON i.category_id = c.category_id " +
                       "LEFT JOIN LOCATION l ON i.location_id = l.location_id " +
                       // Only join CLAIM for non-lost items — CLAIM rows on lost items are match reports
                       "LEFT JOIN CLAIM cl ON cl.item_id = i.item_id AND i.status != 'lost' " +
                       "LEFT JOIN USER cu ON cl.claimer_id = cu.user_id " +
                       "GROUP BY i.item_id " +
                       "ORDER BY i.reported_date DESC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Item item = mapResultSetToItem(rs);
                item.setClaimerNames(rs.getString("claimer_names")); // may be null
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /** Returns items filtered by status with claimer names. Used by the Dashboard Found tab. */
    public List<Item> getItemsWithClaimersByStatus(String status) {
        String query = "SELECT i.*, u.name as reporter_name, c.category_name, l.location_name, " +
                       "GROUP_CONCAT(cu.name ORDER BY cu.name SEPARATOR ', ') as claimer_names " +
                       "FROM ITEM i " +
                       "LEFT JOIN USER u  ON i.user_id      = u.user_id " +
                       "LEFT JOIN CATEGORY c ON i.category_id = c.category_id " +
                       "LEFT JOIN LOCATION l ON i.location_id = l.location_id " +
                       "LEFT JOIN CLAIM cl ON cl.item_id = i.item_id " +
                       "LEFT JOIN USER cu ON cl.claimer_id = cu.user_id " +
                       "WHERE i.status = ? " +
                       "GROUP BY i.item_id " +
                       "ORDER BY i.reported_date DESC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Item item = mapResultSetToItem(rs);
                item.setClaimerNames(rs.getString("claimer_names"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean updateStatus(int itemId, String status) {
        String query = "UPDATE ITEM SET status = ? WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Item> fetchItems(String query) {
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setStatus(rs.getString("status"));
        item.setReportedDate(rs.getDate("reported_date"));
        item.setUserId(rs.getInt("user_id"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setLocationId(rs.getInt("location_id"));
        
        item.setReporterName(rs.getString("reporter_name"));
        item.setCategoryName(rs.getString("category_name"));
        item.setLocationName(rs.getString("location_name"));
        item.setImagePath(rs.getString("image_path"));
        return item;
    }

    public List<com.lostandfound.models.Category> getCategories() {
        List<com.lostandfound.models.Category> list = new ArrayList<>();
        String query = "SELECT * FROM CATEGORY ORDER BY category_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new com.lostandfound.models.Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<com.lostandfound.models.Location> getLocations() {
        List<com.lostandfound.models.Location> list = new ArrayList<>();
        String query = "SELECT * FROM LOCATION ORDER BY location_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new com.lostandfound.models.Location(
                    rs.getInt("location_id"),
                    rs.getString("location_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
