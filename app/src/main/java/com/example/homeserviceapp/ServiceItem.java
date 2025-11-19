package com.example.homeserviceapp;

public class ServiceItem {
    private String title;
    private int price;
    private float rating;
    private int reviewCount;
    private int imageResource;

    public ServiceItem(String title, int price, float rating, int reviewCount, int imageResource) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageResource = imageResource;
    }

    public String getTitle() { return title; }
    public int getPrice() { return price; } // Sửa kiểu trả về
    public float getRating() { return rating; }
    public int getReviewCount() { return reviewCount; } // Thêm getter
    public int getImageResource() { return imageResource; }
}