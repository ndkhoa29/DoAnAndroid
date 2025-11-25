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

        ImageButton btnBack = findViewById(R.id.btnBack);
        // ID: btnSave (Nút Save Changes)
        Button btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();

            finish();
        });
    }
}
