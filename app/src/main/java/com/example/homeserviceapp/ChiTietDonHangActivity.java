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

        ImageButton btnBack = findViewById(R.id.btnBack);

        RelativeLayout providerContainer = findViewById(R.id.providerContainer);

        btnBack.setOnClickListener(v -> {
            finish();
        });


//        providerContainer.setOnClickListener(v -> {
//            Intent intent = new Intent(ChiTietDonHangActivity.this, HoSoActivity.class);
//            startActivity(intent);
//        });


    }

    }
