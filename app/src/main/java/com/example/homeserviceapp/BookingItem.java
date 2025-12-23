package com.example.homeserviceapp;

import com.google.firebase.Timestamp;

public class BookingItem {

    private String bookingId;
    private String serviceId;
    private String serviceName;

    private String scheduleTime;   // ví dụ: "08:00 - 10:00"
    private Timestamp scheduleDate;

    private String location;
    private String price;

    private String status; // pending | confirmed | cancelled | completed

    private int imageResId; // dùng cho RecyclerView local

    // 🔹 BẮT BUỘC cho Firestore
    public BookingItem() {
    }

    // 🔹 Constructor đầy đủ
    public BookingItem(String bookingId, String serviceId, String serviceName,
                       Timestamp scheduleDate, String scheduleTime,
                       String location, String price, String status, int imageResId) {

        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.scheduleDate = scheduleDate;
        this.scheduleTime = scheduleTime;
        this.location = location;
        this.price = price;
        this.status = status;
        this.imageResId = imageResId;
    }

    // ===== GETTERS =====
    public String getBookingId() {
        return bookingId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Timestamp getScheduleDate() {
        return scheduleDate;
    }

    // 🔥 FIX LỖI 3
    public String getScheduleTime() {
        return scheduleTime;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getStatus() {
        return status;
    }

    // ===== STATUS CHECK =====
    // 🔥 FIX LỖI 1
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    // 🔥 FIX LỖI 2
    public boolean isConfirmed() {
        return "confirmed".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }

    // ===== SETTERS (Firestore cần) =====
    public void setStatus(String status) {
        this.status = status;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public void setScheduleDate(Timestamp scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
}
