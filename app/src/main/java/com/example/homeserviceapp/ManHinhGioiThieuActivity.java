package com.example.homeserviceapp;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ManHinhGioiThieuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_hinh_gioi_thieu);

        // ID: btnGetStarted
        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        // Chuyển từ Giới thiệu -> Chi tiết Đơn hàng
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(ManHinhGioiThieuActivity.this, ChiTietDonHangActivity.class);
            startActivity(intent);
            finish();
        });
    }
}