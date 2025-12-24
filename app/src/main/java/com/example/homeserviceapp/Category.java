package com.example.homeserviceapp;

public class Category {
    private String id;
    private String name;
    private String iconUrl; // URL ảnh từ Firebase Storage hoặc tên icon

    public Category() {} // Bắt buộc cho Firebase

    public Category(String id, String name, String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public String getName() { return name; }
    public String getIconUrl() { return iconUrl; }
}