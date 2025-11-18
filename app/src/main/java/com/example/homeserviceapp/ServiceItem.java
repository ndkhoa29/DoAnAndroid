package com.example.homeserviceapp;

public class ServiceItem {

    private String title;
    private int price;
    private float rating;
    private int reviewCount;
    private int imageResource;

    // BỔ SUNG: Thuộc tính để lọc theo danh mục (Category)
    private String categoryType;

    /**
     * Constructor mới bao gồm categoryType.
     * @param title Tên dịch vụ
     * @param price Giá (int)
     * @param rating Đánh giá
     * @param reviewCount Số lượng đánh giá
     * @param imageResource ID tài nguyên hình ảnh
     * @param categoryType Loại danh mục (ví dụ: "Dọn dẹp", "Sửa chữa")
     */
    public ServiceItem(String title, int price, float rating, int reviewCount, int imageResource, String categoryType) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageResource = imageResource;
        this.categoryType = categoryType; // Khởi tạo thuộc tính mới
    }

    // --- Getters ---

    public String getTitle() { return title; }
    public int getPrice() { return price; }
    public float getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public int getImageResource() { return imageResource; }

    // BỔ SUNG: Getter cho Category Type
    public String getCategoryType() { return categoryType; }
}