package com.example.homeserviceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChinhSuaHoSoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_ho_so);

        // ID: btnBack
        ImageButton btnBack = findViewById(R.id.btnBack);
        // ID: btnSave (Nút Save Changes)
        Button btnSave = findViewById(R.id.btnSave);

        // Nút Back: Quay lại màn hình Hồ sơ
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Nút Save: Xử lý lưu và quay lại
        btnSave.setOnClickListener(v -> {
            // Thêm logic lưu dữ liệu tại đây
            Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();

            // Quay lại màn hình Hồ sơ
            finish();
        });
    }
}
