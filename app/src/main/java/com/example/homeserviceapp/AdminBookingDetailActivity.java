package com.example.homeserviceapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Booking;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.example.homeserviceapp.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;

public class AdminBookingDetailActivity extends AppCompatActivity {

    private ImageView ivCustomerAvatar, ivServiceImage;
    private TextView tvCustomerName, tvCustomerPhone;
    private TextView tvServiceName, tvServicePrice;
    private TextView tvBookingId, tvSchedule, tvAddress, tvTotalPrice;
    private TextView tvPaymentStatus, tvPaymentMethod, tvNotes;
    private Button btnApprove, btnComplete, btnCancel;
    
    private FirebaseFirestore db;
    private String bookingId;
    private Booking currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        db = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("BOOKING_ID");

        initViews();
        setupListeners();
        
        if (bookingId != null) {
            loadBookingData();
        } else {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private TextView tvCreatedDate;

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ivCustomerAvatar = findViewById(R.id.ivCustomerAvatar);
        ivServiceImage = findViewById(R.id.ivServiceImage);
        
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvNotes = findViewById(R.id.tvNotes);
        
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        
        btnApprove = findViewById(R.id.btnApprove);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupListeners() {
        btnApprove.setOnClickListener(v -> updateBookingStatus("InProgress"));
        btnComplete.setOnClickListener(v -> updateBookingStatus("TaskCompleted"));
        btnCancel.setOnClickListener(v -> updateBookingStatus("Cancelled"));
    }

    private void loadBookingData() {
        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener(documentSnapshot -> {
                currentBooking = documentSnapshot.toObject(Booking.class);
                if (currentBooking != null) {
                    displayBookingData();
                    setupActionButtons();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void displayBookingData() {
        // Customer Info
        tvCustomerName.setText(currentBooking.getUserName() != null ? currentBooking.getUserName() : "Khách hàng");
        tvCustomerPhone.setText(currentBooking.getUserPhone() != null ? currentBooking.getUserPhone() : "SĐT ẩn");
        
        // Avatar: Not in Booking model, set placeholder
        ivCustomerAvatar.setImageResource(R.drawable.img_profile_placeholder);

        // Service Info
        tvServiceName.setText(currentBooking.getServiceName());
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String priceStr = currencyFormat.format(currentBooking.getPrice()) + " đ";
        tvServicePrice.setText(priceStr);
        tvTotalPrice.setText(priceStr);
        
        if (currentBooking.getServiceImage() != null && !currentBooking.getServiceImage().isEmpty()) {
            Glide.with(this).load(currentBooking.getServiceImage()).into(ivServiceImage);
        } else {
            ivServiceImage.setImageResource(R.drawable.img_office_cleaning);
        }

        // Booking Details
        // Booking Details
        tvBookingId.setText(currentBooking.getCode() != null ? currentBooking.getCode() : currentBooking.getBookingId());
        tvSchedule.setText(currentBooking.getBookingTime() + " - " + currentBooking.getBookingDate());
        tvAddress.setText(currentBooking.getAddress());
        
        // Display Created Date
        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (currentBooking.getCreatedAt() != null) {
             tvCreatedDate.setText(dateFormat.format(currentBooking.getCreatedAt()));
        }

        // Fetch User Avatar
        if (currentBooking.getUserId() != null) {
            db.collection("users").document(currentBooking.getUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String avatarUrl = documentSnapshot.getString("avatarUrl");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this).load(avatarUrl).into(ivCustomerAvatar);
                    } else {
                        ivCustomerAvatar.setImageResource(R.drawable.img_profile_placeholder);
                    }
                });
        }
        
        // Payment Info (Simplified)
        String status = currentBooking.getStatus();
        if ("Paid".equals(status)) {
            tvPaymentStatus.setText("Đã thanh toán");
            tvPaymentStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvPaymentStatus.setText("Chưa thanh toán");
            tvPaymentStatus.setTextColor(Color.parseColor("#FF5722"));
        }
        
        tvPaymentMethod.setText("N/A"); // Method not saved yet
        
        // Notes: Not in Booking model yet, hide or show default
        tvNotes.setText("Không có ghi chú");
        tvNotes.setTextColor(Color.GRAY);
    }

    private void setupActionButtons() {
        if (currentBooking == null) return;
        String status = currentBooking.getStatus();
        
        btnApprove.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        
        // Logic mapping
        if ("Pending".equals(status)) {
            btnApprove.setText("Xác nhận");
            btnApprove.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if ("InProgress".equals(status)) {
            btnComplete.setText("Xong việc");
            btnComplete.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE); // Allow cancel/abort?
        } else if ("TaskCompleted".equals(status)) {
            // Waiting for payment
        }
    }

    private void updateBookingStatus(String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        db.collection("bookings").document(bookingId).update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                
                // Send notification to user
                sendBookingUpdateNotificationToUser(newStatus);
                
                // Reload data to refresh UI
                loadBookingData(); 
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void sendBookingUpdateNotificationToUser(String newStatus) {
        if (currentBooking == null || currentBooking.getUserId() == null) return;
        
        final String userId = currentBooking.getUserId();
        final String bookingCode = currentBooking.getCode() != null ? currentBooking.getCode() : currentBooking.getBookingId();
        
        // Determine notification message based on status
        NotificationData notifData = getNotificationData(newStatus, bookingCode);
        if (notifData == null) return; // Don't send notification for unhandled status
        
        // Get admin's name
        String currentAdminId = FirebaseAuth.getInstance().getUid();
        if (currentAdminId == null) {
            // Fallback: send notification without admin name
            saveNotificationToFirestore(userId, notifData.title, notifData.message, notifData.type, bookingCode);
            return;
        }
        
        final NotificationData finalNotifData = notifData;
        db.collection("users").document(currentAdminId).get()
            .addOnSuccessListener(documentSnapshot -> {
                String adminName = documentSnapshot.getString("fullName");
                if (adminName == null || adminName.isEmpty()) {
                    adminName = "Admin";
                }
                
                // Add admin name to message
                String finalMessage = finalNotifData.message + " bởi " + adminName;
                saveNotificationToFirestore(userId, finalNotifData.title, finalMessage, finalNotifData.type, bookingCode);
            })
            .addOnFailureListener(e -> {
                // Fallback: send without admin name
                saveNotificationToFirestore(userId, finalNotifData.title, finalNotifData.message, finalNotifData.type, bookingCode);
            });
    }
    
    private static class NotificationData {
        String title;
        String message;
        String type;
        
        NotificationData(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
        }
    }
    
    private NotificationData getNotificationData(String status, String bookingCode) {
        switch (status) {
            case "InProgress":
                return new NotificationData(
                    "Đơn hàng đã được xác nhận",
                    "Đơn hàng " + bookingCode + " - " + currentBooking.getServiceName() + " đang được xử lý",
                    "booking_confirmed"
                );
            case "TaskCompleted":
                return new NotificationData(
                    "Dịch vụ đã hoàn thành",
                    "Đơn hàng " + bookingCode + " đã hoàn thành. Vui lòng thanh toán",
                    "booking_completed"
                );
            case "Cancelled":
                return new NotificationData(
                    "Đơn hàng đã bị hủy",
                    "Đơn hàng " + bookingCode + " đã bị hủy bởi admin",
                    "booking_cancelled"
                );
            default:
                return null;
        }
    }
    
    private void saveNotificationToFirestore(String userId, String title, String message, String type, String relatedId) {
        Notification notification = 
            new Notification(
                userId,
                title,
                message,
                type,
                relatedId
            );
        
        db.collection("notifications").add(notification)
            .addOnSuccessListener(documentReference -> {
                Log.d("AdminBookingNotif", "Notification sent to user");
            })
            .addOnFailureListener(e -> {
                Log.e("AdminBookingNotif", "Failed to send notification: " + e.getMessage());
            });
    }
}
