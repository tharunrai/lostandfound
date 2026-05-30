package com.lostandfound.models;

import java.sql.Date;

public class Item {
    private int itemId;
    private String title;
    private String description;
    private String status;
    private Date reportedDate;
    private int userId;
    private int categoryId;
    private int locationId;

    // Optional: for joining data to display
    private String reporterName;
    private String categoryName;
    private String locationName;
    private String claimerNames; // comma-separated list of claimer names
    private String imagePath; // path to the uploaded image

    public Item() {}

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getReportedDate() { return reportedDate; }
    public void setReportedDate(Date reportedDate) { this.reportedDate = reportedDate; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getClaimerNames() { return claimerNames; }
    public void setClaimerNames(String claimerNames) { this.claimerNames = claimerNames; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
