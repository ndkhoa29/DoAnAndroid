package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        ImageView imgBack = findViewById(R.id.img_back);
        MaterialButton btnContinue = findViewById(R.id.btn_continue);

        imgBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(VerificationActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }
}