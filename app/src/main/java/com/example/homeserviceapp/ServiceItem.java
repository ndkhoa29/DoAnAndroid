package com.example.homeserviceapp;

public class ServiceItem {

    private String title;
    private int price;
    private float rating;
    private int reviewCount;

    // THAY ĐỔI: Chuyển từ int (Resource ID) sang String (URL ảnh trên mạng)
    private String imageUrl;

    private String categoryType;

    /**
     * BẮT BUỘC: Constructor trống (no-argument constructor).
     * Firebase Firestore cần cái này để có thể dùng hàm .toObject(ServiceItem.class)
     */
    public ServiceItem() {
    }

    /**
     * Constructor đầy đủ để bạn vẫn có thể tạo object thủ công nếu muốn.
     */
    public ServiceItem(String title, int price, float rating, int reviewCount, String imageUrl, String categoryType) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
        this.categoryType = categoryType;
    }

    // --- Getters ---
    public String getTitle() { return title; }
    public int getPrice() { return price; }
    public float getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryType() { return categoryType; }

    // --- Setters (QUAN TRỌNG) ---
    // Firestore cần các hàm Setters để đổ dữ liệu từ database vào các biến
    public void setTitle(String title) { this.title = title; }
    public void setPrice(int price) { this.price = price; }
    public void setRating(float rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
}