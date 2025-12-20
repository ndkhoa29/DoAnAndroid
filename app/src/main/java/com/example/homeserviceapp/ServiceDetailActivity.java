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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ServiceDetailActivity extends AppCompatActivity {

    ImageView btnChat, btnCall, btnBack;
    Button btnBookNow;

    private String serviceId;
    private String serviceName;
    private String servicePrice;
    private String providerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        serviceId = getIntent().getStringExtra("serviceId");
        serviceName = getIntent().getStringExtra("serviceName");
        servicePrice = getIntent().getStringExtra("servicePrice");
        providerName = getIntent().getStringExtra("providerName");

        if (serviceId == null || serviceId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết dịch vụ");
        }

        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceDetailActivity.this, MessageActivity.class);
            startActivity(intent);
        });

        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+1800123456"));
            startActivity(intent);
        });

        btnBookNow.setOnClickListener(v -> {
            try {
                Log.d("ServiceDetail", "Bắt đầu chuyển sang CalendarActivity");

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(ServiceDetailActivity.this,
                            "Vui lòng đăng nhập để đặt dịch vụ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ServiceDetailActivity.this, CalendarActivity.class);

                intent.putExtra("serviceId", serviceId);
                intent.putExtra("service_name", serviceName != null ? serviceName : "Dịch vụ");
                intent.putExtra("service_price", servicePrice != null ? servicePrice : "0");
                intent.putExtra("provider_name", providerName != null ? providerName : "");

                startActivity(intent);
                Log.d("ServiceDetail", "Đã gọi startActivity");
            } catch (Exception e) {
                Log.e("ServiceDetail", "Lỗi: " + e.getMessage());
                Toast.makeText(ServiceDetailActivity.this,
                        "Lỗi: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}