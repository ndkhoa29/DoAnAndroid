package com.example.homeserviceapp.models;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Review {
    private String reviewId;
    private String serviceId;
    private String customerId;
    private String bookingId;
    private Map<String, Object> customerInfo;
    private double rating;
    private String comment;
    private List<String> imageUrls;
    private boolean isVerifiedBooking;
    private Map<String, Object> adminResponse;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Empty constructor for Firestore
    public Review() {}

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public Map<String, Object> getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(Map<String, Object> customerInfo) {
        this.customerInfo = customerInfo;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public boolean isVerifiedBooking() {
        return isVerifiedBooking;
    }

    public void setVerifiedBooking(boolean verifiedBooking) {
        isVerifiedBooking = verifiedBooking;
    }

    public Map<String, Object> getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(Map<String, Object> adminResponse) {
        this.adminResponse = adminResponse;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean hasAdminResponse() {
        return adminResponse != null && !adminResponse.isEmpty();
    }

    public String getCustomerName() {
        return customerInfo != null ? (String) customerInfo.get("name") : "";
    }
}
