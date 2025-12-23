package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvCustomerName;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ view
        tvCustomerName = findViewById(R.id.tvCustomerName);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu người dùng
        loadUserData();

        // Click vào Header → sang Hồ sơ
        findViewById(R.id.headerSection).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HoSoActivity.class));
        });
    }

    private void loadUserData() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            db.collection("users")
                    .document(userId)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) return;

                        if (value != null && value.exists()) {
                            String fullName = value.getString("fullName");
                            tvCustomerName.setText(fullName);
                        }
                    });
        }
    }
}
