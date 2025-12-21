package com.example.homeserviceapp.models;

import com.google.firebase.Timestamp;

public class BannerItem {
    private String bannerId;
    private String imageUrl;
    private String title;
    private int displayOrder;
    private String linkType; // "none", "service", "category"
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public BannerItem() {}

    // Getters
    public String getBannerId() { return bannerId; }
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public int getDisplayOrder() { return displayOrder; }
    public String getLinkType() { return linkType; }
    public Timestamp getStartDate() { return startDate; }
    public Timestamp getEndDate() { return endDate; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setBannerId(String bannerId) { this.bannerId = bannerId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTitle(String title) { this.title = title; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
