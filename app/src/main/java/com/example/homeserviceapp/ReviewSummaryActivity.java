package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ReviewSummaryActivity extends AppCompatActivity {

    private static final int REQUEST_PAYMENT_METHOD = 100;

    private ImageView btnBack;
    private ImageView imgService;
    private TextView tvServiceName;
    private TextView tvAddress;
    private TextView tvDate;
    private TextView tvTime;
    private CardView cardPaymentMethod;
    private TextView tvPaymentMethodName;
    private RadioButton rbGooglePay;
    private TextView tvPrice;
    private TextView tvTax;
    private TextView tvTotalPrice;
    private Button btnPayNow;

    private double price = 75000;
    private double tax = 10.000;
    private double totalPrice = 65.000;
    private String selectedPaymentMethod = "Ví VNPay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_summary);

        // Thêm callback để xử lý back button và back gesture
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // tương đương super.onBackPressed() + finish()
            }
        });

        initViews();
        setupListeners();
        loadData();
    }


    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgService = findViewById(R.id.imgService);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        cardPaymentMethod = findViewById(R.id.cardPaymentMethod);
        tvPaymentMethodName = findViewById(R.id.tvPaymentMethodName);
        rbGooglePay = findViewById(R.id.rbGooglePay);
        tvPrice = findViewById(R.id.tvPrice);
        tvTax = findViewById(R.id.tvTax);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnPayNow = findViewById(R.id.btnPayNow);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Click vào payment method card để mở màn hình chọn phương thức thanh toán
        cardPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPaymentMethodScreen();
            }
        });

        // Click vào radio button cũng mở màn hình payment method
        rbGooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPaymentMethodScreen();
            }
        });

        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    private void loadData() {
        // Load data from Intent or database
        // For demo purposes, using hardcoded values
        tvServiceName.setText("Dọn dẹp văn phòng");
        tvAddress.setText("15 Trưng Nữ Vương, Hải Châu, Đà Nẵng");
        tvDate.setText("16 tháng 11, 2023");
        tvTime.setText("08:00 AM");

        // Format prices
        tvPrice.setText(String.format("$%.2f", price));
        tvTax.setText(String.format("$%.2f", tax));
        tvTotalPrice.setText(String.format("$%.2f", totalPrice));

        // Set default payment method
        tvPaymentMethodName.setText(selectedPaymentMethod);
        rbGooglePay.setChecked(true);
    }

    private void openPaymentMethodScreen() {
        Intent intent = new Intent(ReviewSummaryActivity.this, PaymentMethodActivity.class);
        intent.putExtra("total_amount", totalPrice);
        intent.putExtra("payment_method", selectedPaymentMethod);
        startActivityForResult(intent, REQUEST_PAYMENT_METHOD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PAYMENT_METHOD && resultCode == RESULT_OK) {
            if (data != null) {
                // Nhận phương thức thanh toán được chọn từ PaymentMethodActivity
                selectedPaymentMethod = data.getStringExtra("payment_method");
                boolean paymentSuccess = data.getBooleanExtra("payment_success", false);

                // Cập nhật UI
                if (selectedPaymentMethod != null) {
                    tvPaymentMethodName.setText(selectedPaymentMethod);
                    Toast.makeText(this, "Đã chọn: " + selectedPaymentMethod,
                            Toast.LENGTH_SHORT).show();
                }

                // Nếu đã thanh toán thành công từ màn hình Payment Method
                if (paymentSuccess) {
                    Toast.makeText(this, "Thanh toán thành công!",
                            Toast.LENGTH_LONG).show();
                    // Navigate to success screen
                    // Intent intent = new Intent(this, PaymentSuccessActivity.class);
                    // startActivity(intent);
                    // finish();
                }
            }
        }
    }

    private void processPayment() {
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        Toast.makeText(this, "Đang xử lý thanh toán qua " + selectedPaymentMethod + "...",
                Toast.LENGTH_SHORT).show();

        // Simulate payment processing delay
        btnPayNow.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show booking completed dialog
                showBookingCompletedDialog();
            }
        }, 1500); // 1.5 seconds delay
    }

    private void showBookingCompletedDialog() {
        BookingCompletedDialog.show(this, new BookingCompletedDialog.OnDialogActionListener() {
            @Override
            public void onGoToHome() {
                // Navigate to home screen
                Intent intent = new Intent(ReviewSummaryActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onViewBooking() {
                // Navigate to booking details screen
                // Intent intent = new Intent(ReviewSummaryActivity.this, BookingDetailsActivity.class);
                // startActivity(intent);
                Toast.makeText(ReviewSummaryActivity.this, "Xem chi tiết đặt dịch vụ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}