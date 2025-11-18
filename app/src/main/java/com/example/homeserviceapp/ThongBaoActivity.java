package com.example.homeserviceapp;


import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ThongBaoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);
        ImageView btnQuayLai = findViewById(R.id.btnQuayLai);

        // Xử lý nút Back: Quay lại màn hình trước
        btnQuayLai.setOnClickListener(v -> {
            finish();
        });
    }
}

