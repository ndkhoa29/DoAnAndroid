package com.example.homeserviceapp.models;

import java.util.Date;

public class Booking {
    private String bookingId;
    private String userId;
    private String userName;
    private String userPhone;
    private String serviceId; // Optional, if we want to track specific service ID
    private String serviceName;
    private String serviceImage;
    private double price;
    private String bookingDate;
    private String bookingTime;
    private String address;
    private String providerName;
    private String status; // Pending, Completed, Cancelled
    private Date createdAt;

    private String code; // Readable Booking Code (e.g. #DH123456)
    private boolean hasReview; // Has user submitted a review


    public Booking() {}

    public Booking(String bookingId, String userId, String userName, String userPhone, 
                   String serviceName, String serviceImage, double price, 
                   String bookingDate, String bookingTime, String address, String providerName,
                   String status, Date createdAt, String code) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.serviceName = serviceName;
        this.serviceImage = serviceImage;
        this.price = price;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.address = address;
        this.providerName = providerName;
        this.status = status;
        this.createdAt = createdAt;
        this.code = code;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceImage() { return serviceImage; }
    public void setServiceImage(String serviceImage) { this.serviceImage = serviceImage; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean getHasReview() { return hasReview; }
    public void setHasReview(boolean hasReview) { this.hasReview = hasReview; }
}
