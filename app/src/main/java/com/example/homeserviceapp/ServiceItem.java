package com.example.homeserviceapp;

import com.google.firebase.firestore.PropertyName;

public class ServiceItem {
    private String title;
    private int price;
    private float rating;
    private int reviewCount;
    private String imageUrl;
    private String categoryType; // Biến này sẽ ánh xạ với "categoryId" trên DB

    public ServiceItem() {}

    // Dùng Annotation để khớp với tên trường thực tế trên Firestore
    @PropertyName("categoryId")
    public String getCategoryType() { return categoryType; }

    @PropertyName("categoryId")
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }

    // --- Các Getter/Setter khác giữ nguyên ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}