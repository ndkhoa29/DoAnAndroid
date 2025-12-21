package com.example.homeserviceapp;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class HoSoActivity extends AppCompatActivity {

    private de.hdodenhof.circleimageview.CircleImageView anhDaiDien;
    private EditText nhapHoVaTen, nhapEmail, nhapSoDienThoai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ho_so);

        anhDaiDien = findViewById(R.id.anhDaiDien);
        nhapHoVaTen = findViewById(R.id.nhapHoVaTen);
        nhapEmail = findViewById(R.id.nhapEmail);
        nhapSoDienThoai = findViewById(R.id.nhapSoDienThoai);

        nhapHoVaTen.setFocusable(false);
        nhapHoVaTen.setFocusableInTouchMode(false);
        nhapEmail.setFocusable(false);
        nhapEmail.setFocusableInTouchMode(false);
        nhapSoDienThoai.setFocusable(false);
        nhapSoDienThoai.setFocusableInTouchMode(false);

        ImageButton nutQuayLai = findViewById(R.id.nutQuayLai);
        ImageButton nutChinhSua = findViewById(R.id.nutChinhSua);

        nutQuayLai.setOnClickListener(v -> {
            finish();
        });

        nutChinhSua.setOnClickListener(v -> {
            Intent intent = new Intent(HoSoActivity.this, ChinhSuaHoSoActivity.class);
            startActivity(intent);
        });

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String avatarUrl = documentSnapshot.getString("avatarUrl");

                        if (fullName != null && !fullName.isEmpty()) {
                            nhapHoVaTen.setText(fullName);
                        }

                        if (email != null && !email.isEmpty()) {
                            nhapEmail.setText(email);
                        }

                        if (phone != null && !phone.isEmpty()) {
                            nhapSoDienThoai.setText(phone);
                        }

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            String optimizedUrl = avatarUrl;
                            if (avatarUrl.contains("cloudinary.com")) {
                                optimizedUrl = com.example.homeserviceapp.helpers.CloudinaryHelper.getThumbnailUrl(avatarUrl);
                            }
                            
                            com.bumptech.glide.Glide.with(this)
                                .load(optimizedUrl)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                .skipMemoryCache(false)
                                .placeholder(R.drawable.ic_provider_avatar)
                                .error(R.drawable.ic_provider_avatar)
                                .into(anhDaiDien);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
        }
    }
}
