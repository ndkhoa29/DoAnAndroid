package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private MaterialButton btnContinue;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        ImageView imgBack = findViewById(R.id.img_back);
        etEmail = findViewById(R.id.etEmail);
        btnContinue = findViewById(R.id.btn_continue);
        progressBar = findViewById(R.id.progressBar);

        imgBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnContinue.setEnabled(false);

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();

                    new android.os.Handler().postDelayed(() -> finish(), 2000);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnContinue.setEnabled(true);

                    String errorMessage = "Gửi email thất bại";
                    if (e.getMessage().contains("no user record")) {
                        errorMessage = "Email không tồn tại trong hệ thống";
                    } else if (e.getMessage().contains("network error")) {
                        errorMessage = "Lỗi kết nối mạng";
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                });
    }
}