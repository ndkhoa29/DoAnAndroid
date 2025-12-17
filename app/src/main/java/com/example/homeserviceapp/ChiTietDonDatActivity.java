package com.example.homeserviceapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ChiTietDonDatActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_dat);

        btnBack = findViewById(R.id.btnBack);
        btnBooking = findViewById(R.id.btnBooking);

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút đặt lịch
        btnBooking.setOnClickListener(v -> showBookingSuccessDialog());
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