package com.example.homeserviceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReviewSummaryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView imgService;
    private TextView tvServiceName;
    private TextView tvAddress;
    private TextView tvDate;
    private TextView tvTime;
    private RadioButton rbGooglePay;
    private TextView tvPrice;
    private TextView tvTax;
    private TextView tvTotalPrice;
    private Button btnPayNow;

    private double price = 30.00;
    private double tax = 10.00;
    private double totalPrice = 40.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_summary);

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

        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });

        rbGooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReviewSummaryActivity.this,
                        "Google Pay selected", Toast.LENGTH_SHORT).show();
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
        rbGooglePay.setChecked(true);
    }

    private void processPayment() {
        if (rbGooglePay.isChecked()) {
            // Show loading dialog
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();

            // Here you would integrate with payment gateway
            // For demo, just show success message
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();

            // Navigate to success screen or finish
            // Intent intent = new Intent(this, PaymentSuccessActivity.class);
            // startActivity(intent);
            // finish();
        } else {
            Toast.makeText(this, "Please select a payment method",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}