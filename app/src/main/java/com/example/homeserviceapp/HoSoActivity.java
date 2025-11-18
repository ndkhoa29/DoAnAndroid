package com.example.homeserviceapp;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class HoSoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ho_so);

        // ID: nutQuayLai
        ImageButton nutQuayLai = findViewById(R.id.nutQuayLai);
        // ID: nutChinhSua
        ImageButton nutChinhSua = findViewById(R.id.nutChinhSua);

        // Nút Back: Quay lại màn hình trước
        nutQuayLai.setOnClickListener(v -> {
            finish();
        });

        // Nút Chỉnh sửa: Chuyển đến màn hình Chỉnh sửa Hồ sơ
        nutChinhSua.setOnClickListener(v -> {
            Intent intent = new Intent(HoSoActivity.this, ChinhSuaHoSoActivity.class);
            startActivity(intent);
        });
    }
}
