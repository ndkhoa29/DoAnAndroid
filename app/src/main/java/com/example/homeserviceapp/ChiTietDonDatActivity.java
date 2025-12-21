package com.example.homeserviceapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class ChiTietDonDatActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnBooking;
    
    // UI Components
    private TextView tvServiceName, tvServiceDate, tvServiceTime, tvServiceLocation;
    private TextView tvPrice, tvTax, tvTotalPrice, tvOrderId, tvOrderTime, tvCustomerName, tvCustomerPhone, tvProviderName;
    
    // Data
    private String serviceName;
    private String serviceId;
    private String bookingDate;
    private String bookingTime;
    private String bookingAddress;
    private String serviceImage; // New Image field
    private double price = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_dat);
        
        // Receive Data
        Intent intent = getIntent();
        serviceName = intent.getStringExtra("SERVICE_NAME");
        serviceId = intent.getStringExtra("SERVICE_ID");
        bookingDate = intent.getStringExtra("BOOKING_DATE");
        bookingTime = intent.getStringExtra("BOOKING_TIME");
        bookingAddress = intent.getStringExtra("BOOKING_ADDRESS");
        serviceImage = intent.getStringExtra("SERVICE_IMAGE"); // Get Image
        
        String priceStr = intent.getStringExtra("SERVICE_PRICE");
        // ... (price parsing logic remains same)
        if (priceStr != null) {
            try {
                String cleanPrice = priceStr.replaceAll("[^\\d]", "");
                price = Double.parseDouble(cleanPrice);
            } catch (NumberFormatException e) {
                price = 0;
            }
        }

        initViews();
        displayData();

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút đặt lịch
        btnBooking.setOnClickListener(v -> saveBookingToFirestore());
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBooking = findViewById(R.id.btnBooking);
        
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceDate = findViewById(R.id.tvServiceDate);
        tvServiceTime = findViewById(R.id.tvServiceTime);
        tvServiceLocation = findViewById(R.id.tvServiceLocation);
        
        // Views for new details
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderTime = findViewById(R.id.tvOrderTime);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvProviderName = findViewById(R.id.tvProviderName);
        
        // Price Views
        tvPrice = findViewById(R.id.tvPrice);
        tvTax = findViewById(R.id.tvTax);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        
        // Image View (Added)
        // Ensure ID matches XML: android:id="@+id/imgService"
        findViewById(R.id.imgService); 
    }
    
    // Helper to get global Glide context if needed, but here simple usage:
    
    private void displayData() {
        // ... (Previous data setting logic) ...
        // 1. Order Info
        // 1. Order Info
        // Generate random but readable 6-digit code
        long randomNum = 100000 + (long)(Math.random() * 900000);
        String orderCode = "#DH" + randomNum;
        tvOrderId.setText("Mã đơn hàng: " + orderCode);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());
        tvOrderTime.setText("Thời gian đặt: " + currentTime);
        
        // 2. Customer Info
        com.example.homeserviceapp.helpers.UserPreferences userPrefs = 
            new com.example.homeserviceapp.helpers.UserPreferences(this);
        String uName = userPrefs.getUserName();
        String uPhone = userPrefs.getUserPhone();
        if (uName.isEmpty()) uName = "Khách hàng";
        if (uPhone == null || uPhone.isEmpty()) uPhone = "Chưa cập nhật";
        tvCustomerName.setText(uName);
        tvCustomerPhone.setText(uPhone);
        
        // 3. Service Info
        if (serviceName != null) tvServiceName.setText(serviceName);
        if (bookingDate != null) tvServiceDate.setText(bookingDate);
        if (bookingTime != null) tvServiceTime.setText(bookingTime);
        if (bookingAddress != null) tvServiceLocation.setText(bookingAddress);
        
        // LOAD IMAGE
        if (serviceImage != null && !serviceImage.isEmpty()) {
            android.widget.ImageView imgService = findViewById(R.id.imgService);
            com.bumptech.glide.Glide.with(this)
                .load(serviceImage)
                .placeholder(R.drawable.img_office_cleaning) // Default placeholder
                .error(R.drawable.img_office_cleaning)
                .into(imgService);
        }
        
        // 4. Provider Info
        tvProviderName.setText("Service Center AZ");
        
        // 5. Pricing
        double tax = price * 0.1; // 10% tax
        double total = price + tax;
        
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        
        tvPrice.setText(currencyFormat.format(price) + " đ");
        tvTax.setText(currencyFormat.format(tax) + " đ");
        tvTotalPrice.setText(currencyFormat.format(total) + " đ");
    }

    private void saveBookingToFirestore() {
        // Show loading state (simple toast for now, or disable button)
        btnBooking.setEnabled(false);
        btnBooking.setText("Đang xử lý...");

        // Get Firestore instance
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        // Get User ID from auth or preferences (Assuming user is logged in)
        // Ideally use FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Fallback to caching preference if needed, but Auth is better for security
        final String userId;
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        } else {
            userId = "";
        }
        
        final String finalServiceName = serviceName;

        // Create Booking Object
        String bookingId = db.collection("bookings").document().getId();
        
        // Use the code displayed to the user
        String finalCode = tvOrderId.getText().toString().replace("Mã đơn hàng: ", "");
        
        com.example.homeserviceapp.models.Booking booking = new com.example.homeserviceapp.models.Booking();
        booking.setBookingId(bookingId);
        booking.setCode(finalCode); // Set standardised code
        booking.setUserId(userId);
        booking.setUserName(tvCustomerName.getText().toString());
        booking.setUserPhone(tvCustomerPhone.getText().toString());
        booking.setServiceId(serviceId); // CRITICAL: Save serviceId for reviews!
        booking.setServiceName(serviceName);
        booking.setServiceImage(serviceImage);
        booking.setPrice(price);
        booking.setBookingDate(bookingDate);
        booking.setBookingTime(bookingTime);
        booking.setAddress(bookingAddress);
        booking.setProviderName("Service Center AZ");
        booking.setStatus("Pending"); // Default status
        booking.setCreatedAt(new java.util.Date());

        // Save to Firestore
        db.collection("bookings").document(bookingId)
            .set(booking)
            .addOnSuccessListener(aVoid -> {
                // Success - Send notification to admins
                sendBookingNotificationToAdmins(userId, finalCode, finalServiceName);
                showBookingSuccessDialog();
            })
            .addOnFailureListener(e -> {
                // Failure
                btnBooking.setEnabled(true);
                btnBooking.setText("Đặt lịch");
                Toast.makeText(ChiTietDonDatActivity.this, "Lỗi khi đặt lịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void sendBookingNotificationToAdmins(String userId, String bookingCode, String serviceName) {
        com.google.firebase.firestore.FirebaseFirestore db = 
            com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        // Get user's fullName first
        db.collection("users").document(userId).get()
            .addOnSuccessListener(userDoc -> {
                String userName = userDoc.getString("fullName");
                if (userName == null || userName.isEmpty()) {
                    userName = "Khách hàng";
                }
                
                final String finalUserName = userName;
                
                // Query all admins and send notification
                db.collection("users")
                    .whereEqualTo("userType", "admin")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        com.example.homeserviceapp.helpers.NotificationHelper notificationHelper = 
                            new com.example.homeserviceapp.helpers.NotificationHelper(this);
                        
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String adminId = doc.getId();
                            
                            String title = "Đơn hàng mới từ " + finalUserName;
                            String message = "Dịch vụ: " + serviceName + " - Mã: " + bookingCode;
                            
                            // Create and save notification
                            com.example.homeserviceapp.models.Notification notification = 
                                new com.example.homeserviceapp.models.Notification(
                                    adminId,
                                    title,
                                    message,
                                    "new_booking",
                                    bookingCode
                                );
                            
                            db.collection("notifications").add(notification);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("BookingNotif", "Failed to query admins: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("BookingNotif", "Failed to get user info: " + e.getMessage());
            });
    }

    private void showBookingSuccessDialog() {
        // Tạo dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_completed);
        dialog.setCancelable(false);

        // Set background trong suốt để bo góc hiển thị đúng
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Lấy nút về trang chủ
        Button btnGoToHome = dialog.findViewById(R.id.btnGoToHome);
        btnGoToHome.setOnClickListener(v -> {
            dialog.dismiss();

            // Chuyển về trang chủ và xóa toàn bộ back stack
            Intent intent = new Intent(ChiTietDonDatActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}