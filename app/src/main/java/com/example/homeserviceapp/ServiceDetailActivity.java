package com.example.homeserviceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ServiceDetailActivity extends AppCompatActivity {

    ImageView btnChat, btnCall, btnBack;
    Button btnBookNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        // Thêm nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết dịch vụ");
        }

        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceDetailActivity.this, MessageActivity.class);
            startActivity(intent);
        });


        // Nút gọi điện
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+1800123456"));
            startActivity(intent);
        });

        // Nút "Đặt ngay" - Chuyển sang CalendarActivity
//        btnBookNow.setOnClickListener(v -> {
//            Intent intent = new Intent(ServiceDetailActivity.this, CalendarActivity.class);
//
//            // Có thể truyền thêm dữ liệu dịch vụ sang trang calendar
//            intent.putExtra("service_name", "Dọn dẹp văn phòng");
//            intent.putExtra("service_price", "75.000");
//            intent.putExtra("provider_name", "Nguyễn Văn A");
//
//            startActivity(intent);
//        });

        btnBookNow.setOnClickListener(v -> {
            try {
                Log.d("ServiceDetail", "Bắt đầu chuyển sang CalendarActivity");

                Intent intent = new Intent(ServiceDetailActivity.this, CalendarActivity.class);
                intent.putExtra("service_name", "Dọn dẹp văn phòng");
                intent.putExtra("service_price", "75.000");
                intent.putExtra("provider_name", "Nguyễn Văn A");

                startActivity(intent);
                Log.d("ServiceDetail", "Đã gọi startActivity");
            } catch (Exception e) {
                Log.e("ServiceDetail", "Lỗi: " + e.getMessage());
                Toast.makeText(ServiceDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Xử lý sự kiện khi nhấn nút Back trên ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}