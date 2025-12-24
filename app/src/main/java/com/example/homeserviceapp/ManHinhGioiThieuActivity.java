package com.example.homeserviceapp;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class ManHinhGioiThieuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Check user role from Firestore
            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userType = documentSnapshot.getString("userType");
                    Intent intent;
                    if ("admin".equals(userType)) {
                        intent = new Intent(this, AdminMainActivity.class);
                    } else {
                        intent = new Intent(this, MainActivity.class);
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Default to MainActivity on error
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            return;
        }

        setContentView(R.layout.activity_man_hinh_gioi_thieu);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(ManHinhGioiThieuActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

