package com.example.homeserviceapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.homeserviceapp.models.Booking;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class ChiTietDonHangActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderTime, tvStatus;
    private TextView tvProviderName;
    private TextView tvServiceName, tvServiceLocation, tvServiceDate, tvServiceTime;
    private TextView tvPrice, tvTax, tvTotalPrice;
    private ImageView imgService, imgProvider;
    
    private FirebaseFirestore db;
    private String bookingId;
    private Booking currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_hang);

        db = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("BOOKING_ID");

        initViews();
        
        if (bookingId != null) {
            loadBookingData();
        } else {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderTime = findViewById(R.id.tvOrderTime);
        tvStatus = findViewById(R.id.tvStatus);
        
        tvProviderName = findViewById(R.id.tvProviderName);
        
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceLocation = findViewById(R.id.tvServiceLocation);
        tvServiceDate = findViewById(R.id.tvServiceDate);
        tvServiceTime = findViewById(R.id.tvServiceTime);
        
        tvPrice = findViewById(R.id.tvPrice);
        tvTax = findViewById(R.id.tvTax);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        
        imgService = findViewById(R.id.imgService);
        imgProvider = findViewById(R.id.imgProvider);
        
        // Hide payment method as it's not implemented yet
        View cardPaymentMethod = findViewById(R.id.cardPaymentMethod);
        if (cardPaymentMethod != null) {
            cardPaymentMethod.setVisibility(View.GONE);
        }
    }

    private void loadBookingData() {
        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener(documentSnapshot -> {
                currentBooking = documentSnapshot.toObject(Booking.class);
                if (currentBooking != null) {
                    displayBookingData();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void displayBookingData() {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        
        // Order Info
        tvOrderId.setText("Mã đơn hàng: " + (currentBooking.getCode() != null ? currentBooking.getCode() : currentBooking.getBookingId()));
        
        // Format created date
        if (currentBooking.getCreatedAt() != null) {
            java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd 'Tháng' MM yyyy", Locale.getDefault());
            tvOrderTime.setText("Thời gian đặt: " + dateFormat.format(currentBooking.getCreatedAt()));
        } else {
            tvOrderTime.setText("Thời gian đặt: N/A");
        }
        
        // Status
        String status = currentBooking.getStatus();
        displayStatus(status);
        
        // Provider Info
        tvProviderName.setText(currentBooking.getProviderName() != null ? currentBooking.getProviderName() : "Nhà cung cấp");
        
        // Service Info
        tvServiceName.setText(currentBooking.getServiceName());
        tvServiceLocation.setText(currentBooking.getAddress());
        tvServiceDate.setText(currentBooking.getBookingDate());
        tvServiceTime.setText(currentBooking.getBookingTime());
        
        // Load service image
        if (currentBooking.getServiceImage() != null && !currentBooking.getServiceImage().isEmpty()) {
            Glide.with(this).load(currentBooking.getServiceImage()).into(imgService);
        } else {
            imgService.setImageResource(R.drawable.img_office_cleaning);
        }
        
        // Payment Summary
        double price = currentBooking.getPrice();
        double tax = price * 0.1; // 10% tax (matching ChiTietDonDatActivity)
        double total = price + tax;
        
        tvPrice.setText(currencyFormat.format(price) + " đ");
        tvTax.setText(currencyFormat.format(tax) + " đ");
        tvTotalPrice.setText(currencyFormat.format(total) + " đ");
    }

    private void displayStatus(String status) {
        if ("Pending".equals(status)) {
            tvStatus.setText("Chờ xác nhận");
            tvStatus.setTextColor(Color.parseColor("#FFA000"));
        } else if ("InProgress".equals(status)) {
            tvStatus.setText("Đang thực hiện");
            tvStatus.setTextColor(Color.parseColor("#2196F3"));
        } else if ("TaskCompleted".equals(status)) {
            tvStatus.setText("Chờ thanh toán");
            tvStatus.setTextColor(Color.parseColor("#9C27B0"));
        } else if ("Paid".equals(status)) {
            tvStatus.setText("Hoàn thành");
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("Cancelled".equals(status)) {
            tvStatus.setText("Đã hủy");
            tvStatus.setTextColor(Color.RED);
        } else {
            tvStatus.setText(status);
            tvStatus.setTextColor(Color.GRAY);
        }
    }
}
