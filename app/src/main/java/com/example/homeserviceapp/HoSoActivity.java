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

        ImageButton nutQuayLai = findViewById(R.id.nutQuayLai);
        ImageButton nutChinhSua = findViewById(R.id.nutChinhSua);

        nutQuayLai.setOnClickListener(v -> {
            finish();
        });

        nutChinhSua.setOnClickListener(v -> {
            Intent intent = new Intent(HoSoActivity.this, ChinhSuaHoSoActivity.class);
            startActivity(intent);
        });
    }
}
