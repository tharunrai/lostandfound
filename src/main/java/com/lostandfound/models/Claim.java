package com.lostandfound.models;

import java.sql.Date;

public class Claim {
    private int claimId;
    private int itemId;
    private int claimerId;
    private String claimStatus;
    private Date claimDate;

    // Additional display info
    private String itemTitle;
    private String claimerName;

    public Claim() {}

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getClaimerId() { return claimerId; }
    public void setClaimerId(int claimerId) { this.claimerId = claimerId; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    public Date getClaimDate() { return claimDate; }
    public void setClaimDate(Date claimDate) { this.claimDate = claimDate; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public String getClaimerName() { return claimerName; }
    public void setClaimerName(String claimerName) { this.claimerName = claimerName; }
}
