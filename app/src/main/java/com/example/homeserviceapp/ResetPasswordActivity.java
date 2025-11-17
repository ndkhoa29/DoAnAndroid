package com.example.homeserviceapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ImageView imgBack = findViewById(R.id.img_back);
        MaterialButton btnReset = findViewById(R.id.btn_reset);

        imgBack.setOnClickListener(v -> finish());

        // Khi nhấn nút "Reset Password"
        btnReset.setOnClickListener(v -> {
            // (Sau khi kiểm tra mật khẩu mới và xác nhận mật khẩu khớp nhau)
            showSuccessDialog();
        });
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_password_success);

        // Làm cho nền dialog trong suốt (để thấy bo tròn)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnGoToLogin = dialog.findViewById(R.id.btn_go_to_login);

        btnGoToLogin.setOnClickListener(v -> {
            dialog.dismiss();

            // Quay về màn hình Login và xóa hết các Activity
            // Giả sử Activity Login/Sign up của bạn tên là MainActivity
            Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }
}