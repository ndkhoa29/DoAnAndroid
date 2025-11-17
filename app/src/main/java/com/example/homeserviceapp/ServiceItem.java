package com.example.homeserviceapp;

public class ServiceItem {
    private String title;
    // THAY ĐỔI: Chuyển price từ String thành int (hoặc double)
    private int price;
    private float rating;
    private int reviewCount; // THÊM MỚI: Để sắp xếp theo "nhiều đánh giá nhất"
    private int imageResource;

    public ServiceItem(String title, int price, float rating, int reviewCount, int imageResource) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageResource = imageResource;
    }

    // Getters
    public String getTitle() { return title; }
    public int getPrice() { return price; } // Sửa kiểu trả về
    public float getRating() { return rating; }
    public int getReviewCount() { return reviewCount; } // Thêm getter
    public int getImageResource() { return imageResource; }
}