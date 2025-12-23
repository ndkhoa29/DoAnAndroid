package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class HoSoActivity extends AppCompatActivity {

    private CircleImageView anhDaiDien;
    private EditText nhapHoVaTen, nhapEmail, nhapSoDienThoai;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ho_so);

        // Ánh xạ view (đúng ID XML mới)
        anhDaiDien = findViewById(R.id.anhDaiDien);
        nhapHoVaTen = findViewById(R.id.nhapHoVaTen);
        nhapEmail = findViewById(R.id.nhapEmail);
        nhapSoDienThoai = findViewById(R.id.nhapSoDienThoai);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Chế độ chỉ xem (Hình 5)
        nhapHoVaTen.setEnabled(false);
        nhapEmail.setEnabled(false);
        nhapSoDienThoai.setEnabled(false);

        ImageButton nutQuayLai = findViewById(R.id.nutQuayLai);
        ImageButton nutChinhSua = findViewById(R.id.nutChinhSua);

        nutQuayLai.setOnClickListener(v -> finish());

        // Sang màn hình chỉnh sửa (Hình 4)
        nutChinhSua.setOnClickListener(v -> {
            startActivity(new Intent(this, ChinhSuaHoSoActivity.class));
        });

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // refresh sau khi chỉnh sửa
    }

    private void loadUserProfile() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) return;

                    String fullName = document.getString("fullName");
                    String email = document.getString("email");
                    String phone = document.getString("phoneNumber");
                    String avatarUrl = document.getString("avatarUrl");

                    if (fullName != null) nhapHoVaTen.setText(fullName);
                    if (email != null) nhapEmail.setText(email);
                    if (phone != null) nhapSoDienThoai.setText(phone);

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        String finalUrl = avatarUrl;

                        if (avatarUrl.contains("cloudinary.com")) {
                            finalUrl = com.example.homeserviceapp.helpers.CloudinaryHelper
                                    .getThumbnailUrl(avatarUrl);
                        }

                        Glide.with(this)
                                .load(finalUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.ic_provider_avatar)
                                .error(R.drawable.ic_provider_avatar)
                                .into(anhDaiDien);
                    }
                });
    }
}
