package com.example.homeserviceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChiTietDonHangActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_hang);

        // 1. Ánh xạ các View
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Ánh xạ container của nhà cung cấp dịch vụ (ID đã được thêm trong XML)
        RelativeLayout providerContainer = findViewById(R.id.providerContainer);

        // 2. Thiết lập sự kiện lắng nghe (Listeners)

        // Nút Back: Quay lại màn hình trước
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Khu vực nhà cung cấp dịch vụ: Chuyển sang HoSoActivity
        providerContainer.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietDonHangActivity.this, HoSoActivity.class);
            startActivity(intent);
        });


    }

    }
