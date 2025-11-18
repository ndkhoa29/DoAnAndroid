package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ImageView imgBack = findViewById(R.id.img_back);
        MaterialButton btnContinue = findViewById(R.id.btn_continue);

        imgBack.setOnClickListener(v -> finish()); // Quay lại màn hình trước

        btnContinue.setOnClickListener(v -> {
            // Chuyển sang màn hình Verification
            Intent intent = new Intent(ForgotPasswordActivity.this, VerificationActivity.class);
            // Bạn có thể gửi email qua đây: intent.putExtra("email", email);
            startActivity(intent);
        });
    }
}