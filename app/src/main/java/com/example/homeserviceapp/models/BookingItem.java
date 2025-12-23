package com.example.homeserviceapp.models;

public class BookingItem {
    private String bookingId;
    private String date;
    private String serviceName;
    private String location;
    private String time;
    private String price;
    private int imageResId;

    public BookingItem(String bookingId, String date, String serviceName,
                       String location, String time, String price, int imageResId) {
        this.bookingId = bookingId;
        this.date = date;
        this.serviceName = serviceName;
        this.location = location;
        this.time = time;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getBookingId() { return bookingId; }
    public String getDate() { return date; }
    public String getServiceName() { return serviceName; }
    public String getLocation() { return location; }
    public String getTime() { return time; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }

}